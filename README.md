AndroidSideMenu
===============

AndroidSideMenu lets you create side (slide\drawer, whatever you call it) menu without special effort. 

Installlation
=============

Add .jar file to build path.

Usage
=====

There is no need to extend another Activity, all you have to do is wrap your layout in SlideHolder widget like this:

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
			android:background="@color/your_background_color"
			/>
			
	</com.agimind.widget.SlideHolder>
	
Note, that you should provide fixed width for your menu (in xml or programmaticaly) and explicitly set non-transparent background for your main layour.

License
=======

	Copyright dmitry.zaicew@gmail.com Dmitry Zaitsev

	Licensed under the Apache License, Version 2.0 (the "License");
	you may not use this file except in compliance with the License.
	You may obtain a copy of the License at

		http://www.apache.org/licenses/LICENSE-2.0

	Unless required by applicable law or agreed to in writing, software
	distributed under the License is distributed on an "AS IS" BASIS,
	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
	See the License for the specific language governing permissions and
	limitations under the License.