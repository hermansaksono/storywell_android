package edu.neu.ccs.wellness.miband2;

import android.os.Handler;
import android.util.Log;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

import edu.neu.ccs.wellness.miband2.listeners.NotifyListener;
import edu.neu.ccs.wellness.miband2.model.Profile;
import edu.neu.ccs.wellness.miband2.model.Protocol;

/**
 * This file is part of Gadgetbridge.
 * Copyright (C) 2016-2018 Carsten Pfeiffer
 * Modified by hermansaksono on 6/29/18.
 * Reference: https://medium.com/machine-learning-world/how-i-hacked-xiaomi-miband-2-to-control-it-from-linux-a5bd2f36d3ad
 */

class Pair {

    private static final String TAG = "mi-band-pair";
    private BluetoothIO io;
    private byte[] secretKey;
    private byte[] randomAuthNumber;
    private ActionCallback actionCallback;
    private Handler handler = new Handler();

    public void perform(BluetoothIO miBandIO, ActionCallback actionCallback) {
        this.io = miBandIO;
        this.actionCallback = actionCallback;
        startAuthentication();
    }

    private void startAuthentication() {
        enableAuthNotifications();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                sendEncryptionKey();
            }
        }, MiBand.BTLE_DELAY_MODERATE);
    }

    /* AUTH COMMAND  */
    private void enableAuthNotifications() {
        this.io.setNotifyListener(Profile.UUID_SERVICE_MIB2, Profile.UUID_CHAR_9_AUTH, notifyListener);
    }

    private void sendEncryptionKey() {
        this.secretKey = getSecretKey();
        this.io.writeCharacteristic(Profile.UUID_SERVICE_MIB2, Profile.UUID_CHAR_9_AUTH,
                append(Protocol.COMMAND_AUTH_SEND_KEY, this.secretKey), new ActionCallback() {
                    @Override
                    public void onSuccess(Object data) {
                        Log.d(TAG, "enableAuthNotifications success");
                    }

                    @Override
                    public void onFail(int errorCode, String msg) {
                        Log.d(TAG, "enableAuthNotifications failed: " + msg);
                    }
                });

        byte[] command = append(Protocol.COMMAND_AUTH_SEND_KEY, this.secretKey);
    }

    private void requestRandomAuthNumber() {
        this.io.writeCharacteristic(Profile.UUID_SERVICE_MIB2, Profile.UUID_CHAR_9_AUTH,
                Protocol.COMMAND_REQUEST_RANDOM_AUTH_NUMBER, new ActionCallback() {
                    @Override
                    public void onSuccess(Object data) {
                        Log.d(TAG, "requestRandomAuthNumber success");
                    }

                    @Override
                    public void onFail(int errorCode, String msg) {
                        Log.d(TAG, "requestRandomAuthNumber failed");
                    }
                });
    }

    private void sendEncryptedRandomAuthNumber(byte[] value, byte[] secretKey) {
        this.io.writeCharacteristic(Profile.UUID_SERVICE_MIB2, Profile.UUID_CHAR_9_AUTH,
                getEncryptedRandomKey(value, secretKey), new ActionCallback() {
                    @Override
                    public void onSuccess(Object data) {
                        Log.d(TAG, "sendEncryptedRandomAuthNumber success");
                    }

                    @Override
                    public void onFail(int errorCode, String msg) {
                        Log.d(TAG, "sendEncryptedRandomAuthNumber failed");
                    }
                });

        //Log.d(TAG, String.format("Got number: %s", Arrays.toString(value)));
        //Log.d(TAG, String.format("Got secretKey: %s", Arrays.toString(secretKey)));
        //Log.d(TAG, String.format("Sending encrypted number: %s", Arrays.toString(getEncryptedRandomKey(value, secretKey))));
    }

    /* CHARACTERISTIC CHANGE LISTENER */
    private final NotifyListener notifyListener = new NotifyListener() {
        @Override
        public void onNotify(byte[] data) {
            Log.d("mi-band-pair", String.format("Response: %s", Arrays.toString(data)));
            if (isThisSendEncryptionKeyResponse(data)) {
                requestRandomAuthNumber();
            } else if (isThisRequestRandomKeyResponse(data)) {
                randomAuthNumber = getRandomAuthNumberFromResponse(data);
                sendEncryptedRandomAuthNumber(randomAuthNumber, secretKey);
            } else if (isThisSendEncryptedRandomKeyResponse(data)) {
                actionCallback.onSuccess("Authenticated");
            } else {
                actionCallback.onFail(0, "Auth response from MiBand not recognized");
            }
        }
    };

    private boolean isThisSendEncryptionKeyResponse(byte[] value) {
        return value[0] == Protocol.AUTH_RESPONSE &&
                value[1] == Protocol.AUTH_SEND_KEY &&
                value[2] == Protocol.AUTH_SUCCESS;
    }

    private boolean isThisRequestRandomKeyResponse(byte[] value) {
        return value[0] == Protocol.AUTH_RESPONSE &&
                value[1] == Protocol.AUTH_REQUEST_RANDOM_AUTH_NUMBER &&
                value[2] == Protocol.AUTH_SUCCESS;
    }

    private boolean isThisSendEncryptedRandomKeyResponse(byte[] value) {
        return value[0] == Protocol.AUTH_RESPONSE &&
                value[1] == Protocol.AUTH_SEND_ENCRYPTED_AUTH_NUMBER &&
                value[2] == Protocol.AUTH_SUCCESS;
    }

    private byte[] getRandomAuthNumberFromResponse(byte[] response) {
        return Arrays.copyOfRange(response, 3, 19);
    }

    /* SECRET KEY METHODS*/
    private byte[] getSecretKey() {
        return new byte[]{
                0x30, 0x31, 0x32, 0x33,
                0x34, 0x35, 0x36, 0x37,
                0x38, 0x39, 0x40, 0x41,
                0x42, 0x43, 0x44, 0x45};
    }

    private byte[] getEncryptedRandomKey(byte[] value, byte[] secretKey) {
        byte[] combinedKey = null;
        try {
            byte[] sendKey = Protocol.COMMAND_SEND_ENCRYPTED_AUTH_NUMBER;
            byte[] encrypted = getEncryptedValue(value, secretKey);
            combinedKey = append(sendKey, encrypted);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        }
        return combinedKey;
    }

    private byte[] getEncryptedValue(byte[] value, byte[] secretKey)
            throws InvalidKeyException, NoSuchPaddingException,
            NoSuchAlgorithmException, BadPaddingException, IllegalBlockSizeException {
        Cipher ecipher = Cipher.getInstance("AES/ECB/NoPadding");
        SecretKeySpec newKey = new SecretKeySpec(secretKey, "AES");
        ecipher.init(Cipher.ENCRYPT_MODE, newKey);
        byte[] enc = ecipher.doFinal(value);
        return enc;
    }

    private byte[] append(byte[] a, byte[] b) {
        byte[] output = new byte[a.length + b.length];
        System.arraycopy(a, 0, output, 0, a.length);
        System.arraycopy(b, 0, output, a.length, b.length);
        return output;
    }
}
