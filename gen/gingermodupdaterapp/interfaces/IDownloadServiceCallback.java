/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: /home/simone/workspace/neak-updater/src/gingermodupdaterapp/interfaces/IDownloadServiceCallback.aidl
 */
package gingermodupdaterapp.interfaces;
public interface IDownloadServiceCallback extends android.os.IInterface
{
/** Local-side IPC implementation stub class. */
public static abstract class Stub extends android.os.Binder implements gingermodupdaterapp.interfaces.IDownloadServiceCallback
{
private static final java.lang.String DESCRIPTOR = "gingermodupdaterapp.interfaces.IDownloadServiceCallback";
/** Construct the stub at attach it to the interface. */
public Stub()
{
this.attachInterface(this, DESCRIPTOR);
}
/**
 * Cast an IBinder object into an gingermodupdaterapp.interfaces.IDownloadServiceCallback interface,
 * generating a proxy if needed.
 */
public static gingermodupdaterapp.interfaces.IDownloadServiceCallback asInterface(android.os.IBinder obj)
{
if ((obj==null)) {
return null;
}
android.os.IInterface iin = (android.os.IInterface)obj.queryLocalInterface(DESCRIPTOR);
if (((iin!=null)&&(iin instanceof gingermodupdaterapp.interfaces.IDownloadServiceCallback))) {
return ((gingermodupdaterapp.interfaces.IDownloadServiceCallback)iin);
}
return new gingermodupdaterapp.interfaces.IDownloadServiceCallback.Stub.Proxy(obj);
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
case TRANSACTION_updateDownloadProgress:
{
data.enforceInterface(DESCRIPTOR);
long _arg0;
_arg0 = data.readLong();
int _arg1;
_arg1 = data.readInt();
java.lang.String _arg2;
_arg2 = data.readString();
java.lang.String _arg3;
_arg3 = data.readString();
java.lang.String _arg4;
_arg4 = data.readString();
this.updateDownloadProgress(_arg0, _arg1, _arg2, _arg3, _arg4);
reply.writeNoException();
return true;
}
case TRANSACTION_UpdateDownloadMirror:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
this.UpdateDownloadMirror(_arg0);
reply.writeNoException();
return true;
}
case TRANSACTION_DownloadFinished:
{
data.enforceInterface(DESCRIPTOR);
gingermodupdaterapp.customTypes.UpdateInfo _arg0;
if ((0!=data.readInt())) {
_arg0 = gingermodupdaterapp.customTypes.UpdateInfo.CREATOR.createFromParcel(data);
}
else {
_arg0 = null;
}
this.DownloadFinished(_arg0);
reply.writeNoException();
return true;
}
case TRANSACTION_DownloadError:
{
data.enforceInterface(DESCRIPTOR);
this.DownloadError();
reply.writeNoException();
return true;
}
}
return super.onTransact(code, data, reply, flags);
}
private static class Proxy implements gingermodupdaterapp.interfaces.IDownloadServiceCallback
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
public void updateDownloadProgress(long downloaded, int total, java.lang.String downloadedText, java.lang.String speedText, java.lang.String remainingTimeText) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeLong(downloaded);
_data.writeInt(total);
_data.writeString(downloadedText);
_data.writeString(speedText);
_data.writeString(remainingTimeText);
mRemote.transact(Stub.TRANSACTION_updateDownloadProgress, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
public void UpdateDownloadMirror(java.lang.String mirror) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(mirror);
mRemote.transact(Stub.TRANSACTION_UpdateDownloadMirror, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
public void DownloadFinished(gingermodupdaterapp.customTypes.UpdateInfo u) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
if ((u!=null)) {
_data.writeInt(1);
u.writeToParcel(_data, 0);
}
else {
_data.writeInt(0);
}
mRemote.transact(Stub.TRANSACTION_DownloadFinished, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
public void DownloadError() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_DownloadError, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
}
static final int TRANSACTION_updateDownloadProgress = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
static final int TRANSACTION_UpdateDownloadMirror = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
static final int TRANSACTION_DownloadFinished = (android.os.IBinder.FIRST_CALL_TRANSACTION + 2);
static final int TRANSACTION_DownloadError = (android.os.IBinder.FIRST_CALL_TRANSACTION + 3);
}
public void updateDownloadProgress(long downloaded, int total, java.lang.String downloadedText, java.lang.String speedText, java.lang.String remainingTimeText) throws android.os.RemoteException;
public void UpdateDownloadMirror(java.lang.String mirror) throws android.os.RemoteException;
public void DownloadFinished(gingermodupdaterapp.customTypes.UpdateInfo u) throws android.os.RemoteException;
public void DownloadError() throws android.os.RemoteException;
}
