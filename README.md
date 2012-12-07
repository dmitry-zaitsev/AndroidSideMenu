AndroidSideMenu
===============

AndroidSideMenu lets you create side (slide\drawer, whatever you call it) menu without special effort. There is no need to extend another Activity, all you have to do is wrap your layout in SlideHolder widget like this:

	<com.agimind.widget.SlideHolder
		android:layout_width="match_parent"
		android:layout_height="match_parent">
		
		<!-- Here is any view that will represent your side menu. Don't forget to provide width! -->
		<View 
			android:layout_width="300dp"
			android:layout_height="match_parent"
			/>
		
		<!-- And here is your main layout -->
		<View 
			android:layout_width="match_parent"
			android:layout_height="match_parent"
			/>
			
	</com.agimind.widget.SlideHolder>