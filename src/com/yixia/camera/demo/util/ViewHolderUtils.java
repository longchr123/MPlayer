package com.yixia.camera.demo.util;

import android.util.SparseArray;
import android.view.View;

public class ViewHolderUtils {

	/**
	 * 用法： ImageView bananaView = ViewHolder.get(convertView, R.id.banana);
	 * 
	 * @param convertView
	 * @param id
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T extends View> T getView(View convertView, int id) {
		SparseArray<View> viewHolder = (SparseArray<View>) convertView.getTag();
		if (viewHolder == null) {
			viewHolder = new SparseArray<View>();
			convertView.setTag(viewHolder);
		}
		View childView = viewHolder.get(id);
		if (childView == null) {
			childView = convertView.findViewById(id);
			viewHolder.put(id, childView);
		}
		return (T) childView;
	}

}
