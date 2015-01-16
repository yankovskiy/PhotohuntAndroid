/*******************************************************************************
 * Copyright (C) 2014 Artem Yankovskiy (artemyankovskiy@gmail.com).
 *      This program is free software: you can redistribute it and/or modify
 *      it under the terms of the GNU General Public License as published by
 *      the Free Software Foundation, either version 3 of the License, or
 *      (at your option) any later version.
 *  
 *      This program is distributed in the hope that it will be useful,
 *      but WITHOUT ANY WARRANTY; without even the implied warranty of
 *      MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *      GNU General Public License for more details.
 *  
 *      You should have received a copy of the GNU General Public License
 *      along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/

package ru.neverdark.abs;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.MenuItem;

public abstract class UfoFragment extends Fragment implements CommonApi{
    private ActionBarDrawerToggle mDrawerToggle;
    private boolean mIsBackHandle;
    private boolean mIsChangeNavi;

    public void setChangeNavi(boolean changeNavi) {
        mIsChangeNavi = changeNavi;
    }

    public ActionBarDrawerToggle getDrawerToggle() {
        return mDrawerToggle;
    }

    public void setDrawerToggle(ActionBarDrawerToggle drawerToggle) {
        mDrawerToggle = drawerToggle;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mIsBackHandle) {
            switch (item.getItemId()) {
                case android.R.id.home:
                    FragmentManager fm = getFragmentManager();
                    if (fm.getBackStackEntryCount() > 0) {
                        fm.popBackStack();
                    }

                    return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    public void setBackHandle(boolean isBackHandle) {
        mIsBackHandle = isBackHandle;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mIsChangeNavi) {
            mDrawerToggle.setDrawerIndicatorEnabled(false);
        }
    }

    @Override
    public void onDestroy() {
        if (mIsChangeNavi) {
            mDrawerToggle.setDrawerIndicatorEnabled(true);
        }
        super.onDestroy();
    }
}
