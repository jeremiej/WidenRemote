package com.accenture.cdi.widen.remote;

import org.qeo.EventWriter;
import org.qeo.QeoFactory;
import org.qeo.StateWriter;
import org.qeo.android.QeoAndroid;
import org.qeo.android.QeoConnectionListener;
import org.qeo.exception.QeoException;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.accenture.cdi.widen.Events;
import com.accenture.cdi.widen.data.BookSensor;
import com.accenture.cdi.widen.data.Door;
import com.accenture.cdi.widen.data.TeddySensor;

public class Main extends Activity {

    private QeoFactory qeo = null;
    private WidenQeoConnectionListener wQCL = null;
    private StateWriter<Door> stateWriterDoor = null;
    private EventWriter<TeddySensor> eventWriterTeddyHere = null;
    private EventWriter<BookSensor> eventWriterBookHere = null;

    private TextView stateText = null;
    private Button openDoor = null;
    private Button closeDoor = null;
    private Button teddyHere = null;
    private Button bookHere = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		// loads the buttons and state fields
		findWidgets();

		// create the commands from the buttons
		initCommands();

		// Init Qeo
		initQeo();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	public void onDestroy() {
		super.onDestroy();
       if (stateWriterDoor != null) {
    	   stateWriterDoor.remove(new Door("entree", false));
    	   stateWriterDoor.close();
        }
       if (eventWriterTeddyHere != null) {
    	   eventWriterTeddyHere.close();
        }
       if (eventWriterBookHere != null) {
    	   eventWriterBookHere.close();
        }
        if (qeo != null) {
            qeo.close();
        }
    }

	// Ui methods
	private void findWidgets() {
		this.stateText = (TextView) findViewById(R.id.text_state);
		this.openDoor = (Button) findViewById(R.id.button_open_door);
		this.closeDoor = (Button) findViewById(R.id.button_close_door);
		this.teddyHere = (Button) findViewById(R.id.button_teddy_here);
		this.bookHere = (Button) findViewById(R.id.button_book_here);
	}

	private void initCommands() {
		// Command for "open the door"
		this.openDoor.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				writeDoorState(true);
				updateStateText(R.string.open_the_door);
				
			}
		});
		this.openDoor.setEnabled(false);

		// Command for "close the door"
		this.closeDoor.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				writeDoorState(false);
				updateStateText(R.string.close_the_door);
			}
		});
		this.closeDoor.setEnabled(false);

		// Command for "teddy here"
		this.teddyHere.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				writeEvent(Events.TEDDY_HERE);
			}
		});
		this.teddyHere.setEnabled(false);

		// Command for "book here"
		this.bookHere.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				writeEvent(Events.BOOK_HERE);
			}
		});
		this.bookHere.setEnabled(false);		
	}

	private void updateStateText(int stateTextId) {
		stateText.setText(stateTextId);
	}

	// Qeo methods
	private void initQeo() {
		this.wQCL = new WidenQeoConnectionListener();
		QeoAndroid.initQeo(getApplicationContext(), this.wQCL);
	}

	// Write Door state
	private void writeDoorState(boolean isOpen) {
		this.stateWriterDoor.write(new Door("entree", isOpen));
	}

	// Write events
	private void writeEvent(int eventId) {
		switch(eventId) {
		case Events.TEDDY_HERE:
			this.eventWriterTeddyHere.write(new TeddySensor("peluche"));
			break;
		case Events.BOOK_HERE:
			this.eventWriterBookHere.write(new BookSensor("livre"));
			break;
		}
	}

	private class WidenQeoConnectionListener extends QeoConnectionListener {

	    @Override
	    public void onQeoReady(QeoFactory curQeo)
	    {
	        // Will be called when the Android Qeo Service connection is established and is ready to be used.
	        // This can take a while depending on the security initialization.
	        qeo = curQeo;
	        // This is a good place to create readers and writers
	        try {
				stateWriterDoor = qeo.createStateWriter(Door.class);
				eventWriterTeddyHere = qeo.createEventWriter(TeddySensor.class);
				eventWriterBookHere = qeo.createEventWriter(BookSensor.class);
				openDoor.setEnabled(true);
				closeDoor.setEnabled(true);
				teddyHere.setEnabled(true);
				bookHere.setEnabled(true);
			} catch (QeoException e) {
				e.printStackTrace();
			}
	    }
	
	    @Override
	    public void onQeoClosed(QeoFactory curQeo)
	    {
	    	// Will be called when the Android Qeo Service connection is lost
        }
	 
        @Override
        public void onQeoError(QeoException ex)
        {
            ex.printStackTrace();
	    }

	}

}
