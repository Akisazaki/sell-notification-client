package kr.co.a2cpu.dualcpusellnotifier;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v7.app.NotificationCompat;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.Query;

/**
 * Created by Akisazaki on 2016-04-28.
 * This Class for Notification service
 */
public class NotifyService extends Service implements ChildEventListener {

    private static final String PREFERENCES_KEY_READED_NUMBER = "readed-number";

    private boolean onRunning = false;
    private Firebase firebase;
    private SharedPreferences preferences;
    private Query query;
    private NotificationCompat.Builder builder;

    int getReadedNumber() {
        return preferences.getInt(PREFERENCES_KEY_READED_NUMBER, 0);
    }

    void setReadedNumber(int value) {
        if (getReadedNumber() < value) {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putInt(PREFERENCES_KEY_READED_NUMBER, value);
            editor.commit();
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();

        builder = new NotificationCompat.Builder(this);
        builder.setSmallIcon(R.mipmap.ic_launcher);
        builder.setContentTitle("2cpu");
        builder.setVibrate(new long[]{500, 500, 500, 500});

        preferences = getSharedPreferences(getString(R.string.preferences_name), MODE_PRIVATE);

        Firebase.setAndroidContext(this);
    }

    private void onStart() {
        firebase = new Firebase("https://akafactory.firebaseio.com/2cpu-co-kr/sell");
        query = firebase.orderByChild("number").startAt(getReadedNumber());
        query.addChildEventListener(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (!onRunning) {
            onRunning = true;
            onStart();
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        onRunning = false;
        query.removeEventListener(this);
        super.onDestroy();
    }

    private void buildNotification(SellItem item) {
        builder.setContentText(item.content);

        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(item.link));
        PendingIntent pendingBrowserIntent = PendingIntent.getActivity(this, 0, browserIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(pendingBrowserIntent);

        Notification notification = builder.build();
        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(0x94E42F01 ^ item.number, notification);
    }

    @Override
    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
        SellItem item = dataSnapshot.getValue(SellItem.class);
        if (item.number > getReadedNumber()) {
            setReadedNumber(item.number);
            buildNotification(item);
        }
    }

    @Override
    public void onChildChanged(DataSnapshot dataSnapshot, String s) {
    }

    @Override
    public void onChildMoved(DataSnapshot dataSnapshot, String s) {
    }

    @Override
    public void onChildRemoved(DataSnapshot dataSnapshot) {
    }

    @Override
    public void onCancelled(FirebaseError firebaseError) {
    }
}
