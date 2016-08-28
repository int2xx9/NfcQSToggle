package net.int512.nfcqstoggle;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Icon;
import android.nfc.NfcAdapter;
import android.nfc.NfcManager;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;
import android.widget.Toast;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class NfcToggleTile extends TileService {

    public static class NfcBroadcastReceiver extends BroadcastReceiver {

        private NfcToggleTile tile = null;

        public NfcBroadcastReceiver() {
            throw new UnsupportedOperationException();
        }

        NfcBroadcastReceiver(NfcToggleTile tile) {
            this.tile = tile;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            if (tile == null) return;
            int state = intent.getIntExtra(NfcAdapter.EXTRA_ADAPTER_STATE, -1);
            if (state == NfcAdapter.STATE_OFF) {
                tile.isNfcEnabled = false;
            } else if (state == NfcAdapter.STATE_ON) {
                tile.isNfcEnabled = true;
            }
            tile.updateTileState();
        }
    }

    private static final int NFC_STATE_DISABLE = 0;
    private static final int NFC_STATE_ENABLE = 1;

    private NfcBroadcastReceiver broadcastReceiver = new NfcBroadcastReceiver(this);
    private boolean isNfcEnabled = false;
    private Icon icLeakAdd;
    private Icon icLeakRemove;

    @Override
    public void onCreate() {
        super.onCreate();
        registerReceiver(broadcastReceiver, new IntentFilter(NfcAdapter.ACTION_ADAPTER_STATE_CHANGED));
        NfcManager nfc = (NfcManager)getApplicationContext().getSystemService(Context.NFC_SERVICE);
        isNfcEnabled = nfc.getDefaultAdapter().isEnabled();
        icLeakAdd = Icon.createWithResource(this, R.drawable.ic_leak_add);
        icLeakRemove = Icon.createWithResource(this, R.drawable.ic_leak_remove);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(broadcastReceiver);
    }

    @Override
    public void onClick() {
        super.onClick();
        toggleNfcState();
     }

    private void updateTileState() {
        if (getQsTile() == null) return;
        if (isNfcEnabled) {
            getQsTile().setState(Tile.STATE_ACTIVE);
            getQsTile().setIcon(icLeakAdd);
        } else {
            getQsTile().setState(Tile.STATE_INACTIVE);
            getQsTile().setIcon(icLeakRemove);
        }
        getQsTile().updateTile();
    }

    private void toggleNfcState() {
        if (isNfcEnabled) {
            setNfcState(NFC_STATE_DISABLE);
        } else {
            setNfcState(NFC_STATE_ENABLE);
        }
    }

    private void setNfcState(int newstate) {
        NfcManager nfc = (NfcManager) getApplicationContext().getSystemService(Context.NFC_SERVICE);
        NfcAdapter adapter = nfc.getDefaultAdapter();
        Method setEnable = null, setDisable = null;
        try {
            Class<?> NfcAdapterClass = adapter.getClass();
            setEnable = NfcAdapterClass.getDeclaredMethod("enable");
            setEnable.setAccessible(true);
            setDisable = NfcAdapterClass.getDeclaredMethod("disable");
            setDisable.setAccessible(true);
        } catch (NoSuchMethodException e) {
        }
        try {
            if (newstate == NFC_STATE_ENABLE) {
                setEnable.invoke(adapter);
            } else {
                setDisable.invoke(adapter);
            }
        } catch (InvocationTargetException|IllegalAccessException e) {
            Toast.makeText(getApplicationContext(), getResources().getText(R.string.state_change_error), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onStartListening() {
        updateTileState();
    }

    @Override
    public void onStopListening() {
        updateTileState();
    }

    @Override
    public void onTileAdded() {
        updateTileState();
    }
}
