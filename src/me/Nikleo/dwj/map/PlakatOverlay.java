package me.Nikleo.dwj.map;

import java.util.List;

import android.content.Intent;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import org.osmdroid.DefaultResourceProxyImpl;
import org.osmdroid.api.IMapView;
import org.osmdroid.views.overlay.ItemizedOverlay;


public class PlakatOverlay extends ItemizedOverlay<PlakatOverlayItem> {

	private List<PlakatOverlayItem> mItems;
	private final DwjMap mContext;
	
	public PlakatOverlay(DwjMap context, List<PlakatOverlayItem> items) {
		super(PlakatOverlayItem.getDefaultDrawable(), new DefaultResourceProxyImpl(context));
		mContext = context;
		if (items != null) {
			mItems = items;
			populate();
		}
	}

	@Override
	protected PlakatOverlayItem createItem(int i) {
		return mItems.get(i);
	}

	@Override
	public int size() {
		return mItems.size();
	}

    @Override
    protected boolean onTap(int index) {
    	PlakatOverlayItem item = mItems.get(index);
    	mContext.startActivityForResult(
    			new Intent(mContext, PlakatDetailsActivity.class)
    				.putExtra(PlakatDetailsActivity.EXTRA_PLAKAT_ID, item.getId()),
    				DwjMap.REQUEST_EDIT_PLAKAT);
    	
    	return true;
    }
/*
	public static void Prepare(Drawable[] mIcons) {
		for(int i = 0; i < mIcons.length; i++)
			mIcons[i] = PlakatOverlay. boundCenter(mIcons[i]);
	}
  */

    @Override
    public boolean onSnapToItem(int i, int i2, Point point, IMapView iMapView) {
        return false;
    }
}
