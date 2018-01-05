package com.maocan.router;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import com.maocan.router.annotation.Router;
import java.util.HashMap;

@Router(path = "main")
public class MainActivity extends AppCompatActivity {

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    HashMap<String,String> hashMap = new HashMap();
    hashMap.put("title","content");
    hashMap.put("title1","content1");
    hashMap.put("title2","content2");
  }
}
