package com.sk.thingplug.api

import akka.actor.Props

trait ActorComponent {
  def name: String
  def actorProps(): Props
}