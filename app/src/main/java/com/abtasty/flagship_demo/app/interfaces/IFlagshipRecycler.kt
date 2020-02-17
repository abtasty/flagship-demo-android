package com.abtasty.flagship_demo.app.interfaces

interface IFlagshipRecycler {
    fun onPageClick()
    fun onEventClick()
    fun onTransactionClick()
    fun onItemClick()
    fun onItemClick(position : Int)
}