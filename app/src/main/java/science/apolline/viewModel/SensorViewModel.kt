package science.apolline.viewModel

import science.apolline.service.database.SensorDao
import science.apolline.models.Device
import org.jetbrains.anko.*
import com.github.salomonbrys.kodein.instance
import io.reactivex.Flowable
import science.apolline.root.RootViewModel


class SensorViewModel: RootViewModel<SensorViewModel>(), AnkoLogger {

    private val sensorDao by instance<SensorDao>()

    fun getDeviceListSizeObserver(): Flowable<Long> {
        return sensorDao.getCount()
    }

    fun getDeviceList(nbDevice: Long): Flowable<List<Device>>{
        return sensorDao.getLastEntries(nbDevice)
    }

    fun getDeviceListByDate(dateStart: Long, dateEnd: Long, MAX_DEVICE: Long): Flowable<List<Device>>
    {
        return sensorDao.getEntriesByDate(dateStart, dateEnd, MAX_DEVICE)
    }
}