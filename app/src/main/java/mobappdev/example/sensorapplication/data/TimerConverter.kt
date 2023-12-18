package mobappdev.example.sensorapplication.data

object TimerConverter {

    /**
     * truncates angles to two decimal points.
     */
    fun normalizeAngles(list: List<Float>): List<Float>{
        var normalizedList = mutableListOf<Float>()
        for(item in list){
            var normalizedItem = String.format("%.2f", item).toFloat()
            normalizedList.add(normalizedItem)
        }
        return normalizedList
    }

    /**
     *
     */
    fun normalizeTimestamps(list: List<Long>): List<Long>{
        if(list.isEmpty()) return list
        var normalizedList = mutableListOf<Long>()
        var initialValue:Long=list.first()
        for(item in list){
            var normalizedItem = (item - initialValue) //* Math.pow(10.0,-9.0) //zeros the first number. counts in seconds innstead of nanoseconds, and limits to 4 decimals.
            //val formattedItem = String.format("%.4f", normalizedItem).toDouble()
            normalizedList.add(normalizedItem)
        }
        return normalizedList
    }
}