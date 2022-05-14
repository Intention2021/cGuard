// vim: et sw=4 sts=4 tabstop=4
/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.intention.android.goodmask.issc;

import com.intention.android.goodmask.issc.util.Util;
import android.app.Activity;
import android.os.Environment;
import java.util.UUID;

/**
 * This is the class to store global constant.
 *
 * These constants will be used across classes.
 */

public final class Bluebit {

    /** Tag for Logcat output */
    public final static String TAG = "Bluebit";

    /** Use Fake implementation for testing */
    public final static String USE_FAKE = "USE_FAKE";

    /** Activity Result code */
    // if remote disconnect, the activity should be closed
    public final static int RESULT_REMOTE_DISCONNECT = Activity.RESULT_FIRST_USER + 1;

    // Keys that be used by Intent, Bundle...etc.
    /** The device has been choosen from another activity. */
    public final static String CHOSEN_DEVICE = "the_device_been_choosen";
    /** The path to choose a file for use. */
    public final static String CHOOSE_PATH = "the_path_to_choose_file";

    //public final static String EXTERNAL = "/storage/emulated/legacy";
    public final static String EXTERNAL = Environment.getExternalStorageDirectory().getAbsolutePath();
    public final static String DATA_DIR = EXTERNAL + "/mchp/";
    //public final static String DATA_DIR = "/sdcard/mchp/";   //Sony Xperia X Performance
    //public final static String DATA_DIR = "/storage/MicroSD/mchp";  //ASUS Zenphone2
    public final static String DEFAULT_LOG = DATA_DIR + "received_data";

    // Request-Code for Activities-communication
    private final static int _REQ_START = 0x9527; // just random number
    public final static int REQ_CHOOSE_DEVICE = _REQ_START + 1;

    private Bluebit() {
        // Hide constructor since you should
        // never instantiate me.
    }
    public static int board_id = 70;
    public static int no_burst_mode=0;
    public static int toatal_transactions = 0;
    public static int mtu = 0;
    /* Automation IO service */
    public final static UUID SERVICE_AUTOMATION_IO = UUID.fromString("00001815-0000-1000-8000-00805f9b34fb");
    public final static UUID CHR_DIGITAL_IN  = UUID.fromString("00002a56-0000-1000-8000-00805f9b34fb");
    public final static UUID CHR_DIGITAL_OUT = UUID.fromString("00002a57-0000-1000-8000-00805f9b34fb");
    public final static UUID CHR_ANALOG_IN   = UUID.fromString("00002a58-0000-1000-8000-00805f9b34fb");
    public final static UUID CHR_ANALOG_OUT  = UUID.fromString("00002a59-0000-1000-8000-00805f9b34fb");
    public final static UUID CHR_AGG_INPUT   = UUID.fromString("00002aa0-0000-1000-8000-00805f9b34fb");
    public final static UUID CUSTOM_CHR_DI_DESC = UUID.fromString("49535343-6C1F-401D-BAA3-EC966D1A3AA1");
    public final static UUID CUSTOM_CHR_DO_DESC = UUID.fromString("49535343-F82E-4B2B-847C-DBEA67318E35");
    public final static UUID CUSTOM_CHR_AO1_DESC = UUID.fromString("49535343-A742-442B-9D20-24C6709FBD16");
    public final static UUID CUSTOM_CHR_AI1_DESC = UUID.fromString("49535343-B011-4081-9C96-C3990D17A69E");

    public final static UUID DES_USER_DESCRIPTION      = UUID.fromString("00002901-0000-1000-8000-00805f9b34fb");
    public final static UUID DES_DIGITAL_NUMBER        = UUID.fromString("00002909-0000-1000-8000-00805f9b34fb");
    public final static UUID DES_INPUT_TRIGGER_SETTING = UUID.fromString("0000290A-0000-1000-8000-00805f9b34fb");

    /* battery service */
    public final static UUID SERVICE_BATTERY       = UUID.fromString("0000180F-0000-1000-8000-00805f9b34fb");
    public final static UUID CHR_BATTERY_LEVEL     = UUID.fromString("00002A19-0000-1000-8000-00805f9b34fb");

    /* Tx Power service */
    public final static UUID SERVICE_TX_POWER      = UUID.fromString("00001804-0000-1000-8000-00805f9b34fb");
    public final static UUID CHR_TX_POWER_LEVEL    = UUID.fromString("00002A07-0000-1000-8000-00805f9b34fb");

    /* Link Loss service */
    public final static UUID SERVICE_LINK_LOSS     = UUID.fromString("00001803-0000-1000-8000-00805f9b34fb");
    public final static UUID CHR_ALERT_LEVEL       = UUID.fromString("00002A06-0000-1000-8000-00805f9b34fb");

    /* Immediate Alert service */
    public final static UUID SERVICE_IMMEDIATE_ALERT = UUID.fromString("00001802-0000-1000-8000-00805f9b34fb");

    /* Device Info service */
    public final static UUID SERVICE_DEVICE_INFO     = UUID.fromString("0000180A-0000-1000-8000-00805f9b34fb");
    public final static UUID CHR_MANUFACTURE_NAME   = UUID.fromString("00002A29-0000-1000-8000-00805f9b34fb");
    public final static UUID CHR_MODEL_NUMBER       = UUID.fromString("00002A24-0000-1000-8000-00805f9b34fb");
    public final static UUID CHR_SERIAL_NUMBER      = UUID.fromString("00002A25-0000-1000-8000-00805f9b34fb");
    public final static UUID CHR_HARDWARE_REVISION  = UUID.fromString("00002A27-0000-1000-8000-00805f9b34fb");
    public final static UUID CHR_FIRMWARE_REVISION  = UUID.fromString("00002A26-0000-1000-8000-00805f9b34fb");
    public final static UUID CHR_SOFTWARE_REVISION  = UUID.fromString("00002A28-0000-1000-8000-00805f9b34fb");

    /* ISSC Proprietary */
    public  static  UUID SERVICE_ISSC_PROPRIETARY  = UUID.fromString("49535343-FE7D-4AE5-8FA9-9FAFD205E455");
    public  static  UUID CHR_CONNECTION_PARAMETER  = UUID.fromString("49535343-6DAA-4D02-ABF6-19569ACA69FE");
    public  static  UUID CHR_ISSC_TRANS_TX         = UUID.fromString("49535343-1E4D-4BD9-BA61-23C647249616");
    public  static  UUID CHR_ISSC_TRANS_RX         = UUID.fromString("49535343-8841-43F4-A8D4-ECBE34729BB3");
    public final static  UUID CHR_ISSC_MP               = UUID.fromString("49535343-ACA3-481C-91EC-D85E28A60318");
    public  static  UUID CHR_ISSC_TRANS_CTRL         = UUID.fromString("49535343-4C8A-39B3-2F49-511CFF073B7E");

    /*Set in UUID settings to predefined value else use above value
    public final static  UUID SERVICE_ISSC_PROPRIETARY_VALUE  = UUID.fromString("NULL");
    public final static  UUID CHR_ISSC_TRANS_TX_VALUE         =  UUID.fromString("NULL");
    public final static  UUID CHR_ISSC_TRANS_RX_VALUE         =  UUID.fromString("NULL");*/


    public final static  UUID SERVICE_ISSC_AIR_PATCH_SERVICE  = UUID.fromString("49535343-C9D0-CC83-A44A-6FE238D06D33");
    public final static  UUID CHR_AIR_PATCH             	  = UUID.fromString("49535343-ACA3-481C-91EC-D85E28A60318");

    public final static byte[] CMD_WRITE_E2PROM = {(byte)0x0b};
    public final static byte[] CMD_READ_E2PROM  = {(byte)0x0a};
    public final static byte[] CMD_WRITE_MEMORY = {(byte)0x09};
    public final static byte[] ADDR_E2PROM_NAME = {(byte)0x00, (byte)0x0b};
    public final static byte[] ADDR_MEMORY_NAME = {(byte)0x4e, (byte)0x0b};
    public final static byte CMD_READ_MTU = 0x24;
    public final static int NAME_MAX_SIZE = 16;


    /* Client Characteristic Configuration Descriptor */
    public final static UUID DES_CLIENT_CHR_CONFIG = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

    public final static UUID[] UUIDS_OF_LIGHTING = {
        SERVICE_AUTOMATION_IO,
        CHR_DIGITAL_OUT,
        CHR_ANALOG_OUT,
        CUSTOM_CHR_AO1_DESC,
        SERVICE_ISSC_PROPRIETARY
    };

    public final static UUID[] UUIDS_OF_TRANSPARENT = {
        SERVICE_ISSC_PROPRIETARY,
        CHR_ISSC_TRANS_TX,
        CHR_ISSC_TRANS_RX
    };

}



