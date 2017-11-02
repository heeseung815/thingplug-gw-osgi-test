package com.sk.thingplug.api

/**
  * Created by kylee on 2017. 9. 14..
  */
trait Service{
  def className: String
  def bundleName: String
}

trait ImportService[-In, +Out, +Mat, Setting] extends Service with GraphSource[Out, Mat, Setting] with GraphFlow[In, Out, Mat]

trait BidImportService [-I1, +O1, -I2, +O2, +Mat, Setting] extends Service with GraphSource[O1, Mat, Setting]  with GraphBidiFlow[I1, O1, I2, O2,  Mat] with GraphSink[I2, Mat, Setting]

trait ExportService[-In, +Out, +Mat, Setting] extends Service with GraphFlow[In, Out, Mat] with GraphSink[In, Mat, Setting]

trait BidExportService [-I1, +O1, -I2, +O2, +Mat, Setting] extends Service with GraphSource[O1, Mat, Setting] with GraphBidiFlow[I1, O1, I2, O2,  Mat] with GraphSink[I2, Mat, Setting]
//trait BidExportService [-I1, +O1, -I2, +O2, +Mat, Setting] extends ExportService[I2, O2, Mat, Setting]  with GraphSource[O1, Mat] with GraphBidiFlow[I1, O1, I2, O2,  Mat]

trait FlowService[-In, +Out, +Mat, Setting] extends Service with GraphFlow[In, Out, Mat]