/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: /home/simone/workspace/neak-updater/src/gingermodupdaterapp/interfaces/IUpdateCheckServiceCallback.aidl
 */
package gingermodupdaterapp.interfaces;
public interface IUpdateCheckServiceCallback extends android.os.IInterface
{
/** Local-side IPC implementation stub class. */
public static abstract class Stub extends android.os.Binder implements gingermodupdaterapp.interfaces.IUpdateCheckServiceCallback
{
private static final java.lang.String DESCRIPTOR = "gingermodupdaterapp.interfaces.IUpdateCheckServiceCallback";
/** Construct the stub at attach it to the interface. */
public Stub()
{
this.attachInterface(this, DESCRIPTOR);
}
/**
 * Cast an IBinder object into an gingermodupdaterapp.interfaces.IUpdateCheckServiceCallback interface,
 * generating a proxy if needed.
 */
public static gingermodupdaterapp.interfaces.IUpdateCheckServiceCallback asInterface(android.os.IBinder obj)
{
if ((obj==null)) {
return null;
}
android.os.IInterface iin = (android.os.IInterface)obj.queryLocalInterface(DESCRIPTOR);
if (((iin!=null)&&(iin instanceof gingermodupdaterapp.interfaces.IUpdateCheckServiceCallback))) {
return ((gingermodupdaterapp.interfaces.IUpdateCheckServiceCallback)iin);
}
return new gingermodupdaterapp.interfaces.IUpdateCheckServiceCallback.Stub.Proxy(obj);
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
case TRANSACTION_UpdateCheckFinished:
{
data.enforceInterface(DESCRIPTOR);
this.UpdateCheckFinished();
reply.writeNoException();
return true;
}
}
return super.onTransact(code, data, reply, flags);
}
private static class Proxy implements gingermodupdaterapp.interfaces.IUpdateCheckServiceCallback
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
public void UpdateCheckFinished() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_UpdateCheckFinished, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
}
static final int TRANSACTION_UpdateCheckFinished = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
}
public void UpdateCheckFinished() throws android.os.RemoteException;
}
