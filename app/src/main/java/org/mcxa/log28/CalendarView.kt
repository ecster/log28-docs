package org.mcxa.log28


import android.opengl.Visibility
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.raizlabs.android.dbflow.runtime.DirectModelNotifier
import com.raizlabs.android.dbflow.structure.BaseModel
import kotlinx.android.synthetic.main.fragment_calendar_view.*
import pl.rafman.scrollcalendar.contract.MonthScrollListener
import pl.rafman.scrollcalendar.data.CalendarDay
import java.util.*


/**
 * A simple [Fragment] subclass.
 * Use the [CalendarView.newInstance] factory method to
 * create an instance of this fragment.
 */
class CalendarView : Fragment() {
    lateinit var modelChangeListener: DirectModelNotifier.ModelChangedListener<DayData>
    lateinit var periodDates: List<Long>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_calendar_view, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        periodDates = AppDatabase.getPeriodDatesForMonth(Calendar.getInstance().get(Calendar.YEAR),
                Calendar.getInstance().get(Calendar.MONTH))

        scrollCalendar.setDateWatcher({
            year, month, day ->
            if ((year.toLong() * 10000) + (month.toLong() * 100) + day.toLong() in periodDates) {
                Log.d("CALVIEW", "Period found at " + year.toString() + " " + month.toString())
                CalendarDay.SELECTED
            } else CalendarDay.DEFAULT
        })

        scrollCalendar.setMonthScrollListener(object : MonthScrollListener {
            override fun shouldAddNextMonth(lastDisplayedYear: Int, lastDisplayedMonth: Int): Boolean {
                //TODO prevent going too far into the future
                return true
            }

            override fun shouldAddPreviousMonth(firstDisplayedYear: Int, firstDisplayedMonth: Int): Boolean {
                return true
            }
        })

        modelChangeListener = object: DirectModelNotifier.ModelChangedListener<DayData> {
            override fun onTableChanged(tableChanged: Class<*>?, action: BaseModel.Action) {
                //We don't care
            }

            override fun onModelChanged(model: DayData, action: BaseModel.Action) {
                if (action == BaseModel.Action.INSERT || action == BaseModel.Action.UPDATE) {

                    Log.d("CALVIEW", "Model changed, redrawing calendar")

                    if (model.physicalBleeding) periodDates += model.date
                    else if (model.date in periodDates) periodDates -= model.date
                    Log.d("CALVIEW", periodDates.toString())

                    scrollCalendar.adapter.notifyDataSetChanged()
                }
            }
        }

        DirectModelNotifier.get().registerForModelChanges(DayData::class.java, modelChangeListener)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        DirectModelNotifier.get().unregisterForModelChanges(DayData::class.java, modelChangeListener)
    }

    companion object {

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @return A new instance of fragment CalendarView.
         */
        // TODO: Rename and change types and number of parameters
        fun newInstance(): CalendarView {
            val fragment = CalendarView()
            val args = Bundle()
            fragment.arguments = args
            return fragment
        }
    }

}