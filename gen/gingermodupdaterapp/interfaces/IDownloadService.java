/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: /home/simone/workspace/neak-updater/src/gingermodupdaterapp/interfaces/IDownloadService.aidl
 */
package gingermodupdaterapp.interfaces;
public interface IDownloadService extends android.os.IInterface
{
/** Local-side IPC implementation stub class. */
public static abstract class Stub extends android.os.Binder implements gingermodupdaterapp.interfaces.IDownloadService
{
private static final java.lang.String DESCRIPTOR = "gingermodupdaterapp.interfaces.IDownloadService";
/** Construct the stub at attach it to the interface. */
public Stub()
{
this.attachInterface(this, DESCRIPTOR);
}
/**
 * Cast an IBinder object into an gingermodupdaterapp.interfaces.IDownloadService interface,
 * generating a proxy if needed.
 */
public static gingermodupdaterapp.interfaces.IDownloadService asInterface(android.os.IBinder obj)
{
if ((obj==null)) {
return null;
}
android.os.IInterface iin = (android.os.IInterface)obj.queryLocalInterface(DESCRIPTOR);
if (((iin!=null)&&(iin instanceof gingermodupdaterapp.interfaces.IDownloadService))) {
return ((gingermodupdaterapp.interfaces.IDownloadService)iin);
}
return new gingermodupdaterapp.interfaces.IDownloadService.Stub.Proxy(obj);
}
public android.os.IBinder asBinder()
{
return this;
}
@Override public boolean onTransact(int code, android.os.Parcel data, android.os.Parcel reply, int flags) throws android.os.RemoteException
{
switch (code)
{
case INTERFACE_TRANSACTION:
{
reply.writeString(DESCRIPTOR);
return true;
}
case TRANSACTION_Download:
{
data.enforceInterface(DESCRIPTOR);
gingermodupdaterapp.customTypes.UpdateInfo _arg0;
if ((0!=data.readInt())) {
_arg0 = gingermodupdaterapp.customTypes.UpdateInfo.CREATOR.createFromParcel(data);
}
else {
_arg0 = null;
}
this.Download(_arg0);
reply.writeNoException();
return true;
}
case TRANSACTION_getCurrentUpdate:
{
data.enforceInterface(DESCRIPTOR);
gingermodupdaterapp.customTypes.UpdateInfo _result = this.getCurrentUpdate();
reply.writeNoException();
if ((_result!=null)) {
reply.writeInt(1);
_result.writeToParcel(reply, android.os.Parcelable.PARCELABLE_WRITE_RETURN_VALUE);
}
else {
reply.writeInt(0);
}
return true;
}
case TRANSACTION_getCurrentMirrorName:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _result = this.getCurrentMirrorName();
reply.writeNoException();
reply.writeString(_result);
return true;
}
case TRANSACTION_DownloadRunning:
{
data.enforceInterface(DESCRIPTOR);
boolean _result = this.DownloadRunning();
reply.writeNoException();
reply.writeInt(((_result)?(1):(0)));
return true;
}
case TRANSACTION_PauseDownload:
{
data.enforceInterface(DESCRIPTOR);
this.PauseDownload();
reply.writeNoException();
return true;
}
case TRANSACTION_cancelDownload:
{
data.enforceInterface(DESCRIPTOR);
this.cancelDownload();
reply.writeNoException();
return true;
}
case TRANSACTION_registerCallback:
{
data.enforceInterface(DESCRIPTOR);
gingermodupdaterapp.interfaces.IDownloadServiceCallback _arg0;
_arg0 = gingermodupdaterapp.interfaces.IDownloadServiceCallback.Stub.asInterface(data.readStrongBinder());
this.registerCallback(_arg0);
reply.writeNoException();
return true;
}
case TRANSACTION_unregisterCallback:
{
data.enforceInterface(DESCRIPTOR);
gingermodupdaterapp.interfaces.IDownloadServiceCallback _arg0;
_arg0 = gingermodupdaterapp.interfaces.IDownloadServiceCallback.Stub.asInterface(data.readStrongBinder());
this.unregisterCallback(_arg0);
reply.writeNoException();
return true;
}
}
return super.onTransact(code, data, reply, flags);
}
private static class Proxy implements gingermodupdaterapp.interfaces.IDownloadService
{
private android.os.IBinder mRemote;
Proxy(android.os.IBinder remote)
{
mRemote = remote;
}
public android.os.IBinder asBinder()
{
return mRemote;
}
public java.lang.String getInterfaceDescriptor()
{
return DESCRIPTOR;
}
public void Download(gingermodupdaterapp.customTypes.UpdateInfo ui) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
if ((ui!=null)) {
_data.writeInt(1);
ui.writeToParcel(_data, 0);
}
else {
_data.writeInt(0);
}
mRemote.transact(Stub.TRANSACTION_Download, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
public gingermodupdaterapp.customTypes.UpdateInfo getCurrentUpdate() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
gingermodupdaterapp.customTypes.UpdateInfo _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_getCurrentUpdate, _data, _reply, 0);
_reply.readException();
if ((0!=_reply.readInt())) {
_result = gingermodupdaterapp.customTypes.UpdateInfo.CREATOR.createFromParcel(_reply);
}
else {
_result = null;
}
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
public java.lang.String getCurrentMirrorName() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
java.lang.String _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_getCurrentMirrorName, _data, _reply, 0);
_reply.readException();
_result = _reply.readString();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
public boolean DownloadRunning() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
boolean _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_DownloadRunning, _data, _reply, 0);
_reply.readException();
_result = (0!=_reply.readInt());
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
public void PauseDownload() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_PauseDownload, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
public void cancelDownload() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_cancelDownload, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
public void registerCallback(gingermodupdaterapp.interfaces.IDownloadServiceCallback cb) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeStrongBinder((((cb!=null))?(cb.asBinder()):(null)));
mRemote.transact(Stub.TRANSACTION_registerCallback, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
public void unregisterCallback(gingermodupdaterapp.interfaces.IDownloadServiceCallback cb) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeStrongBinder((((cb!=null))?(cb.asBinder()):(null)));
mRemote.transact(Stub.TRANSACTION_unregisterCallback, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
}
static final int TRANSACTION_Download = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
static final int TRANSACTION_getCurrentUpdate = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
static final int TRANSACTION_getCurrentMirrorName = (android.os.IBinder.FIRST_CALL_TRANSACTION + 2);
static final int TRANSACTION_DownloadRunning = (android.os.IBinder.FIRST_CALL_TRANSACTION + 3);
static final int TRANSACTION_PauseDownload = (android.os.IBinder.FIRST_CALL_TRANSACTION + 4);
static final int TRANSACTION_cancelDownload = (android.os.IBinder.FIRST_CALL_TRANSACTION + 5);
static final int TRANSACTION_registerCallback = (android.os.IBinder.FIRST_CALL_TRANSACTION + 6);
static final int TRANSACTION_unregisterCallback = (android.os.IBinder.FIRST_CALL_TRANSACTION + 7);
}
public void Download(gingermodupdaterapp.customTypes.UpdateInfo ui) throws android.os.RemoteException;
public gingermodupdaterapp.customTypes.UpdateInfo getCurrentUpdate() throws android.os.RemoteException;
public java.lang.String getCurrentMirrorName() throws android.os.RemoteException;
public boolean DownloadRunning() throws android.os.RemoteException;
public void PauseDownload() throws android.os.RemoteException;
public void cancelDownload() throws android.os.RemoteException;
public void registerCallback(gingermodupdaterapp.interfaces.IDownloadServiceCallback cb) throws android.os.RemoteException;
public void unregisterCallback(gingermodupdaterapp.interfaces.IDownloadServiceCallback cb) throws android.os.RemoteException;
}
