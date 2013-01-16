package com.agimind.sidemenuexample;

import com.agimind.widget.SlideHolder;

import android.os.Bundle;
import android.view.View;
import android.app.Activity;

public class MainActivity extends Activity {

	private SlideHolder mSlideHolder;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		mSlideHolder = (SlideHolder) findViewById(R.id.slideHolder);
		
		/*
		 * toggleView can actually be any view you want.
		 * Here, for simplicity, we're using TextView, but you can
		 * easily replace it with button.
		 * 
		 * Note, when menu opens our textView will become invisible, so
		 * it quite pointless to assign toggle-event to it. In real app
		 * consider using UP button instead. In our case toggle() can be
		 * replaced with open().
		 */
		
		View toggleView = findViewById(R.id.textView);
		toggleView.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				mSlideHolder.toggle();
			}
		});
	}

}
