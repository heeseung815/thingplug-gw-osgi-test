package com.sk.thingplug.api

trait ServiceComponent {
  def name:Option[String]
  def start(): Unit
  def stop(): Unit
}
