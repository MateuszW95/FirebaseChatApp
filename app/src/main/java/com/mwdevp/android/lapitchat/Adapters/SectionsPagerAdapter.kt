package com.mwdevp.android.lapitchat.Adapters

import com.mwdevp.android.lapitchat.Fragments.ChatsFragment
import com.mwdevp.android.lapitchat.Fragments.FriendsFragment
import com.mwdevp.android.lapitchat.Fragments.RequestFragment
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter

class SectionsPagerAdapter(fm:FragmentManager): FragmentPagerAdapter(fm) {
    override fun getItem(position: Int): Fragment? {

        when (position){
            0->{
                val f= RequestFragment()
                return f
            }
            1->{
                val f= ChatsFragment()
                return f
            }
            2->{
                val f= FriendsFragment()
                return f
            }

        }

        return  null
    }

    override fun getCount(): Int {
    return 3
    }

    override fun getPageTitle(position: Int): CharSequence? {
        when (position){
            0-> return "REQUESTS"
            1-> return "CHATS"
            2-> return "FRIENDS"
        }
        return null
    }
}