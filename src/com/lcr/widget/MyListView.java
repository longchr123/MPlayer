package com.lcr.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ListView;

public class MyListView extends ListView implements Pullable{

	public MyListView(Context context) {
		super(context);
	}

	public MyListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}
	
	public MyListView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		// TODO Auto-generated constructor stub
	}

	 @Override
		public boolean canPullDown()
		{
			if (getCount() == 0)
			{
				// û��item��ʱ��Ҳ��������ˢ��
				return true;
			} else if (getFirstVisiblePosition() == 0
					&& getChildAt(0).getTop() >= 0)
			{
				// ����ListView�Ķ�����
				return true;
			} else
				return false;
		}

		@Override
		public boolean canPullUp()
		{
			if (getCount() == 0)
			{
				// û��item��ʱ��Ҳ������������
				return true;
			} else if (getLastVisiblePosition() == (getCount() - 1))
			{
				// �����ײ���
				if (getChildAt(getLastVisiblePosition() - getFirstVisiblePosition()) != null
						&& getChildAt(
								getLastVisiblePosition()
										- getFirstVisiblePosition()).getBottom() <= getMeasuredHeight())
					return true;
			}
			return false;
		}
}
