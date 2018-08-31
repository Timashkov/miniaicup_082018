package utils

import java.io.File
import java.io.PrintWriter

class Logger {

//    private var mLogFile: File = File("/Users/aleksey/projects/agario/log.txt")
//    private var mLogFile: File = File("/home/timashkov/Experience/aicup/log.txt")


    fun writeLog(message: String){
        System.err.println(message)
//        mLogFile.appendText(message + "\n")
    }
}