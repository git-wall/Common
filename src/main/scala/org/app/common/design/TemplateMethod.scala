package org.app.common.design

trait TemplateMethod {
  def before(): Unit = {}
  def now(): Unit
  def after(): Unit = {}
}

