package lilinhong.adapter;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import java.util.List;

public class MainFragmentPagerAdapter extends FragmentPagerAdapter {
    private List<Fragment>fragmentList = null;
    public MainFragmentPagerAdapter(FragmentManager fm,List<Fragment>fragmentList) {
        super(fm);
        this.fragmentList = fragmentList;
    }
    public void setFragmentList(List<Fragment>fragmentList){
        this.fragmentList = fragmentList;
    }

    @Override
    public Fragment getItem(int position) {
        return this.fragmentList.get(position);
    }

    @Override
    public int getCount() {
        return this.fragmentList.size();
    }
}
