package com.xxxink.bridge;
import android.app.Activity;
import android.os.Bundle;
import android.widget.*;
import android.content.Intent;
import android.net.Uri;
import java.io.*;
import fi.iki.elonen.NanoHTTPD;

public class MainActivity extends Activity {
 NanoHTTPD server; File root; TextView status;
 public void onCreate(Bundle b){super.onCreate(b); root=new File(getFilesDir(),"published"); root.mkdirs();
 LinearLayout l=new LinearLayout(this); l.setOrientation(1); l.setPadding(24,24,24,24);
 status=new TextView(this); status.setText("Offline — local preview only"); l.addView(status);
 Button start=new Button(this); start.setText("Start local hosting"); l.addView(start); start.setOnClickListener(v->start());
 Button stop=new Button(this); stop.setText("Stop"); l.addView(stop); stop.setOnClickListener(v->{if(server!=null)server.stop();server=null;status.setText("Offline");});
 Button add=new Button(this);add.setText("Publish selected file locally");l.addView(add);add.setOnClickListener(v->{Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT);i.setType("*/*");i.addCategory(Intent.CATEGORY_OPENABLE);startActivityForResult(i,1);});
 Button open=new Button(this);open.setText("Open website");l.addView(open);open.setOnClickListener(v->startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse("http://127.0.0.1:8080/"))));setContentView(l);}
 void start(){if(server!=null)return;try{server=new NanoHTTPD("127.0.0.1",8080){public Response serve(IHTTPSession s){
 if(s.getMethod()!=Method.GET)return newFixedLengthResponse(Response.Status.METHOD_NOT_ALLOWED,"text/plain","GET only");
 String path=s.getUri(); if(path.equals("/")){StringBuilder h=new StringBuilder("<meta name=viewport content='width=device-width'><h1>XXX!NK local archive</h1>");File[] fs=root.listFiles();if(fs!=null)for(File f:fs)if(f.isFile())h.append("<p><a href='/media/").append(f.getName()).append("'>").append(f.getName()).append("</a>");return newFixedLengthResponse(h.toString());}
 if(!path.matches("/media/[a-f0-9-]+[.][a-z0-9]+"))return newFixedLengthResponse(Response.Status.NOT_FOUND,"text/plain","Not found");
 File f=new File(root,path.substring(7));try{Response r=newFixedLengthResponse(Response.Status.OK,"application/octet-stream",new FileInputStream(f),f.length());r.addHeader("Content-Disposition","attachment; filename="+f.getName());r.addHeader("X-Content-Type-Options","nosniff");return r;}catch(IOException e){return newFixedLengthResponse(Response.Status.NOT_FOUND,"text/plain","Not found");}
 }};server.start();status.setText("Running at http://127.0.0.1:8080");}catch(IOException e){server=null;status.setText(e.toString());}}
 protected void onActivityResult(int r,int c,Intent data){super.onActivityResult(r,c,data);if(r!=1||c!=RESULT_OK||data==null)return;String type=getContentResolver().getType(data.getData());String ext="bin";if(type!=null){String x=android.webkit.MimeTypeMap.getSingleton().getExtensionFromMimeType(type);if(x!=null&&x.matches("[a-z0-9]+"))ext=x;}File f=new File(root,java.util.UUID.randomUUID()+"."+ext);try(InputStream in=getContentResolver().openInputStream(data.getData());OutputStream out=new FileOutputStream(f)){byte[] buf=new byte[65536];int n;while((n=in.read(buf))!=-1)out.write(buf,0,n);status.setText("Imported "+f.getName());}catch(Exception e){f.delete();status.setText(e.toString());}}
 protected void onDestroy(){if(server!=null)server.stop();super.onDestroy();}
}
