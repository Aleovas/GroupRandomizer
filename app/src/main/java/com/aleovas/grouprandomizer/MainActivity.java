package com.aleovas.grouprandomizer;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.menu.ActionMenuItemView;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CheckedTextView;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.Switch;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    DrawerLayout drawer;
    ArrayList<contentContainer> containers;
    static CardAdapter adapter;
    FloatingActionButton fab;
    public enum Gender {Male, Female, Nonbinary}
    static ArrayList<Group> groups=new ArrayList<>();
    static SharedPreferences prefs;
    static int groupIDs;
    static Gson gson;
    static ClipboardManager clipboard;
    @SuppressLint("RestrictedApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addGroup();
            }
        });
        fab.setVisibility(View.GONE);

        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.frame_container, new RandomizerFragment()).commit();
        containers = new ArrayList<>();
        adapter = new CardAdapter(this, containers);
        prefs= PreferenceManager.getDefaultSharedPreferences(this);
        gson=new Gson();
        getGroups();
        //getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch (id){
            case R.id.copy:
                copy(findViewById(R.id.copy));
                break;
        }

        //noinspection SimplifiableIfStatement


        return super.onOptionsItemSelected(item);
    }

    @SuppressLint("RestrictedApi")
    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        Fragment fragment=null;
        boolean list=false;
        ActionMenuItemView copy=findViewById(R.id.copy);
        if (id == R.id.nav_randomizer) {
            fragment=new RandomizerFragment();
            fab.setVisibility(View.GONE);
            copy.setVisibility(View.VISIBLE);
        } else if (id == R.id.nav_list) {
            fragment=new ListFragment();
            fab.setVisibility(View.VISIBLE);
            copy.setVisibility(View.INVISIBLE);
            list=true;
        }

        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        if(fragment==null)fragment=new RandomizerFragment();
        if (fragment != null) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.frame_container, fragment).commit();

        }
        //if(list)updateLists();
        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
    public void randomize(View v){
        containers.clear();
        ArrayList<ArrayList<String>> lists=new ArrayList<>();
        ArrayList<String> males=new ArrayList<>();
        ArrayList<String> females=new ArrayList<>();
        ArrayList<String> names=new ArrayList<>();
        ArrayList<ArrayList<String>> malesS=new ArrayList<>();
        ArrayList<ArrayList<String>> femalesS=new ArrayList<>();
        ArrayList<ArrayList<String>> namesS=new ArrayList<>();
        RadioGroup rg=findViewById(R.id.randGroup);
        Switch randSwitch=findViewById(R.id.randSwitch);
        Spinner spinner=findViewById(R.id.randSpinner);
        EditText editText=findViewById(R.id.editText);
        Switch multiSwitch=findViewById(R.id.multiSwitch);
        LinearLayout multiLayout=findViewById(R.id.multiLayout);
        Boolean groupsExist=false;
        Random rnd=new Random();
        if(multiSwitch.isChecked()){
            int count = 0;
            ArrayList<Integer> sizes=new ArrayList<>();
            int fullSize=0;
            for(int i=0;i<multiLayout.getChildCount();i++){
                if(((CheckBox)multiLayout.getChildAt(i)).isChecked()){
                    count++;
                    int num=groups.get(i).students.size();
                    sizes.add(num);
                    fullSize+=num;
                }
            }

            if(count==0) {
                Snackbar.make(v,"Check some groups",Snackbar.LENGTH_LONG).show();
            }else {
//                int id=0;
//                for(int i=0;i<5;i++)if(((RadioButton)rg.getChildAt(i)).isChecked())id=i;
//                if(id==4){
//
//                }else{
//                    Snackbar.make(v,"Only Custom randomization is implemented for multi-Group",Snackbar.LENGTH_LONG).show();
//                }
                int groupNo= Integer.parseInt(editText.getText().toString());
                boolean[][] split = new boolean[count][groupNo];
                boolean done=false;
                LinearLayout cv=findViewById(R.id.customizerView);
                while(!done){
                    split=new boolean[count][groupNo];
                    ArrayList<Integer> se=new ArrayList<>();
                    for(int i:sizes){
                        se.add(i);
                    }
                    for(int i=0;i<groupNo;i++){
                        int size=Integer.parseInt(((EditText)cv.getChildAt(i).findViewById(R.id.groupSize)).getText().toString());
                        int choice=0;
                        int counter=0;
                        do{
                            choice= rnd.nextInt(count);
                            counter++;
                        }while (se.get(choice)<size&&counter<10);
                        split[choice][i]=true;
                        se.set(choice,se.get(choice)-size);
                    }
                    done=true;
                    for(int i:se){
                        if(i!=0)done=false;
                    }
                }
                for(int x=0;x<count;x++){

                }
                for(int j=0;j<multiLayout.getChildCount();j++) {
                    if (((CheckBox) multiLayout.getChildAt(j)).isChecked()) {
                        names.clear();
                        males.clear();
                        females.clear();
                        ArrayList<Student> students=groups.get(j).students;
                        for(Student s:students) {
                            if(s.gender==Gender.Female)females.add(s.name);else males.add(s.name);
                            names.add(s.name);
                        }
                        Collections.shuffle(females);
                        Collections.shuffle(males);
                        Collections.shuffle(names);
                        malesS.add(new ArrayList<String>(males));
                        femalesS.add(new ArrayList<String>(females));
                        namesS.add(new ArrayList<String>(names));
                    }
                }
                for(int j=0;j<count;j++){
                    names=namesS.get(j);
                    males=malesS.get(j);
                    females=femalesS.get(j);
                    int number=names.size();;
                    int constrained=0;
                    int constrainedNo=0;
                    int nonconstrained=0;
                    int correctedGroupNo=0;
                    for(int i=0;i<groupNo;i++){
                        if(split[j][i])correctedGroupNo++;
                    }
                    main:for(int i=0;i<groupNo;i++){
                        if(!split[j][i])continue main;
                        if(((CheckBox)cv.getChildAt(i).findViewById(R.id.numberCheck)).isChecked()){
                            constrainedNo++;
                            int m=0;
                            int f=0;
                            int size=Integer.parseInt(((EditText)cv.getChildAt(i).findViewById(R.id.groupSize)).getText().toString());
                            ArrayList<String> s=new ArrayList<>();
                            for(int x=0;x<size;x++){
                                if(((CheckBox)cv.getChildAt(i).findViewById(R.id.maleCheck)).isChecked()
                                        &&((SeekBar)cv.getChildAt(i).findViewById(R.id.maleBar)).getProgress()>m){
                                    m++;
                                    String temp=males.get(rnd.nextInt(males.size()));
                                    males.remove(temp);
                                    names.remove(temp);
                                    s.add(temp);
                                }else if(((CheckBox)cv.getChildAt(i).findViewById(R.id.femaleCheck)).isChecked()
                                        &&((SeekBar)cv.getChildAt(i).findViewById(R.id.femaleBar)).getProgress()>f){
                                    f++;
                                    String temp=females.get(rnd.nextInt(females.size()));
                                    females.remove(temp);
                                    names.remove(temp);
                                    s.add(temp);
                                }else {
                                    String temp=names.get(rnd.nextInt(names.size()));
                                    names.remove(temp);
                                    s.add(temp);
                                }
                            }
                            constrained+=s.size();
                            Collections.shuffle(s);
                            if(randSwitch.isChecked()){
                                int hTaker=rnd.nextInt(s.size());
                                s.set(hTaker,s.get(hTaker)+"⁕");
                            }
                            String title=((EditText)cv.getChildAt(i).findViewById(R.id.groupTitle)).getText().toString();
                            if(title.equals(""))title="Group "+(containers.size()+1);
                            containers.add(new contentContainer(title,arrayToString(s)));
                        }
                    }
                    main2:for(int i=0;i<groupNo;i++){
                        if(!((CheckBox)cv.getChildAt(i).findViewById(R.id.numberCheck)).isChecked()){
                            if(!split[j][i])continue main2;
                            int m=0;
                            int f=0;
                            ArrayList<String> s=new ArrayList<>();
                            int size=dist(number-constrained,correctedGroupNo-constrainedNo,nonconstrained);
                            for(int x=0;x<size;x++){
                                if(((CheckBox)cv.getChildAt(i).findViewById(R.id.maleCheck)).isChecked()
                                        &&((SeekBar)cv.getChildAt(i).findViewById(R.id.maleBar)).getProgress()>m){
                                    m++;
                                    String temp=males.get(rnd.nextInt(males.size()));
                                    males.remove(temp);
                                    names.remove(temp);
                                    s.add(temp);
                                }else if(((CheckBox)cv.getChildAt(i).findViewById(R.id.femaleCheck)).isChecked()
                                        &&((SeekBar)cv.getChildAt(i).findViewById(R.id.femaleBar)).getProgress()>f){
                                    f++;
                                    String temp=females.get(rnd.nextInt(females.size()));
                                    females.remove(temp);
                                    names.remove(temp);
                                    s.add(temp);
                                }else {
                                    String temp=names.get(rnd.nextInt(names.size()));
                                    names.remove(temp);
                                    s.add(temp);
                                }
                            }
                            nonconstrained++;
                            Collections.shuffle(s);
                            if(randSwitch.isChecked()){
                                int hTaker=rnd.nextInt(s.size());
                                s.set(hTaker,s.get(hTaker)+"⁕");
                            }
                            String title=((EditText)cv.getChildAt(i).findViewById(R.id.groupTitle)).getText().toString();
                            if(title.equals(""))title="Group "+(containers.size()+1);
                            containers.add(new contentContainer(title,arrayToString(s)));

                        }
                    }
                    adapter.notifyDataSetChanged();
                }

            }
        }else{
            try{
                ArrayList<Student> students=groups.get(spinner.getSelectedItemPosition()).students;
                for(Student s:students) {
                    if(s.gender==Gender.Female)females.add(s.name);else males.add(s.name);
                    names.add(s.name);
                }
                Collections.shuffle(females);
                Collections.shuffle(males);
                Collections.shuffle(names);
                groupsExist=true;
            }catch (Exception e){
                Snackbar.make(v,"Create group from the \"Group Lists\" tab",Snackbar.LENGTH_LONG).show();
            }
            if(groupsExist){
                int id=0;
                for(int i=0;i<5;i++)if(((RadioButton)rg.getChildAt(i)).isChecked())id=i;
                int groupNo=0;
                try{
                    groupNo= Integer.parseInt(editText.getText().toString());
                    int groupSize=names.size()/groupNo;
                    if(groupNo>names.size())throw new Exception();
                    switch (id){
                        case 0:
                            //Snackbar.make(v,"BAL",Snackbar.LENGTH_LONG).show();
                            boolean matriarchy = females.size()>=males.size();
                            if (groupNo == 1) {
                                lists.add(names);
                                break;
                            }else if(groupNo==2){
                                ArrayList<String> s1=new ArrayList<>();
                                ArrayList<String> s2=new ArrayList<>();
                                s1.addAll(males.subList(0,males.size()/2));
                                s2.addAll(females.subList(0,females.size()/2));
                                s2.addAll(males.subList(males.size()/2,males.size()));
                                s1.addAll(females.subList(females.size()/2,females.size()));
                                lists.add(s1);
                                lists.add(s2);
                                break;
                            }
                            for(int i=0;i<groupNo;i++){
                                ArrayList<String> s=new ArrayList<>();
                                int noMales=Math.max((males.size()/names.size())*groupSize,1);
                                int noFemales=Math.max((females.size()/names.size())*groupSize,1);
                                if(matriarchy){
                                    for(int n=0;n<groupSize;n++){
                                        String temp;
                                        if((n<noMales)&&males.size()>0){
                                            temp=males.get(rnd.nextInt(males.size()));
                                            males.remove(temp);

                                        }else if(females.size()>0){
                                            temp=females.get(rnd.nextInt(females.size()));
                                            females.remove(temp);
                                        }
                                        else {
                                            temp=names.get(rnd.nextInt(names.size()));
                                        }
                                        names.remove(temp);
                                        s.add(temp);
                                    }
                                }else{
                                    for(int n=0;n<groupSize;n++){
                                        String temp;
                                        if((n<noFemales)&&females.size()>0){
                                            temp=females.get(rnd.nextInt(females.size()));
                                            females.remove(temp);

                                        }else if(males.size()>0){
                                            temp=males.get(rnd.nextInt(males.size()));
                                            males.remove(temp);
                                        }
                                        else {
                                            temp=names.get(rnd.nextInt(names.size()));
                                        }
                                        names.remove(temp);
                                        s.add(temp);
                                    }
                                }
                                lists.add(s);
                            }

                            for(int i=0;i<names.size();i++)lists.get(i).add(names.get(i));
                            break;
                        case 1:
                            //Snackbar.make(v,">1F",Snackbar.LENGTH_LONG).show();
                            if(females.size()<groupNo)throw new Exception();
                            for(int i=0;i<groupNo;i++){
                                ArrayList<String> s=new ArrayList<>();
                                String t=females.get(rnd.nextInt(females.size()));
                                females.remove(t);
                                names.remove(t);
                                s.add(t);
                                for(int n=1;n<groupSize;n++){
                                    String temp=names.get(rnd.nextInt(names.size()));
                                    while(females.contains(temp) && females.size()<groupNo-i){
                                        temp=names.get(rnd.nextInt(names.size()));
                                    }
                                    names.remove(temp);
                                    if(females.contains(temp))females.remove(temp);
                                    s.add(temp);
                                }
                                lists.add(s);
                            }
                            for(int i=0;i<names.size();i++)lists.get(i).add(names.get(i));
                            break;
                        case 2:
                            //Snackbar.make(v,"<=1M",Snackbar.LENGTH_LONG).show();
//                        if(males.size()>groupNo)throw new Exception();
//                        for(int i=0;i<groupNo;i++){
//                            ArrayList<String> s=new ArrayList<>();
//                            for(int n=0;n<groupSize;n++){
//                                String temp;
//                                if(n==0&&males.size()>0){
//                                    temp=males.get(rnd.nextInt(males.size()));
//                                    males.remove(temp);
//
//                                }else {
//                                    temp=females.get(rnd.nextInt(females.size()));
//                                    females.remove(temp);
//                                }
//                                names.remove(temp);
//                                s.add(temp);
//                            }
//                            lists.add(s);
//                        }
                            if(males.size()<groupNo)throw new Exception();
                            for(int i=0;i<groupNo;i++){
                                ArrayList<String> s=new ArrayList<>();
                                String t=males.get(rnd.nextInt(males.size()));
                                males.remove(t);
                                names.remove(t);
                                s.add(t);
                                for(int n=1;n<groupSize;n++){
                                    String temp=names.get(rnd.nextInt(names.size()));
                                    while(males.contains(temp) && males.size()<groupNo-i){
                                        temp=names.get(rnd.nextInt(names.size()));
                                    }
                                    names.remove(temp);
                                    if(males.contains(temp))males.remove(temp);
                                    s.add(temp);
                                }
                                lists.add(s);
                            }
                            for(int i=0;i<names.size();i++)lists.get(i).add(names.get(i));
                            break;
                        case 3:
                            //Snackbar.make(v,"RAND",Snackbar.LENGTH_LONG).show();
                            for(int i=0;i<groupNo;i++){
                                ArrayList<String> s=new ArrayList<>();
                                for(int n=0;n<groupSize;n++){
                                    String temp=names.get(rnd.nextInt(names.size()));
                                    names.remove(temp);
                                    s.add(temp);
                                }
                                lists.add(s);
                            }
                            for(int i=0;i<names.size();i++)lists.get(i).add(names.get(i));
                            //lists.add(names);
                            break;
                        case 4:
                            int number=names.size();
                            int constrained=0;
                            int constrainedNo=0;
                            int nonconstrained=0;
                            LinearLayout cv=findViewById(R.id.customizerView);
                            for(int i=0;i<groupNo;i++){
                                if(((CheckBox)cv.getChildAt(i).findViewById(R.id.numberCheck)).isChecked()){
                                    constrainedNo++;
                                    int m=0;
                                    int f=0;
                                    int size=Integer.parseInt(((EditText)cv.getChildAt(i).findViewById(R.id.groupSize)).getText().toString());
                                    ArrayList<String> s=new ArrayList<>();
                                    for(int x=0;x<size;x++){
                                        if(((CheckBox)cv.getChildAt(i).findViewById(R.id.maleCheck)).isChecked()
                                                &&((SeekBar)cv.getChildAt(i).findViewById(R.id.maleBar)).getProgress()>m){
                                            m++;
                                            String temp=males.get(rnd.nextInt(males.size()));
                                            males.remove(temp);
                                            names.remove(temp);
                                            s.add(temp);
                                        }else if(((CheckBox)cv.getChildAt(i).findViewById(R.id.femaleCheck)).isChecked()
                                                &&((SeekBar)cv.getChildAt(i).findViewById(R.id.femaleBar)).getProgress()>f){
                                            f++;
                                            String temp=females.get(rnd.nextInt(females.size()));
                                            females.remove(temp);
                                            names.remove(temp);
                                            s.add(temp);
                                        }else {
                                            String temp=names.get(rnd.nextInt(names.size()));
                                            names.remove(temp);
                                            s.add(temp);
                                        }
                                    }
                                    constrained+=s.size();
                                    Collections.shuffle(s);
                                    if(randSwitch.isChecked()){
                                        int hTaker=rnd.nextInt(s.size());
                                        s.set(hTaker,s.get(hTaker)+"⁕");
                                    }
                                    String title=((EditText)cv.getChildAt(i).findViewById(R.id.groupTitle)).getText().toString();
                                    if(title.equals(""))title="Group "+(containers.size()+1);
                                    containers.add(new contentContainer(title,arrayToString(s)));
                                }
                            }
                            for(int i=0;i<groupNo;i++){
                                if(!((CheckBox)cv.getChildAt(i).findViewById(R.id.numberCheck)).isChecked()){
                                    int m=0;
                                    int f=0;
                                    ArrayList<String> s=new ArrayList<>();
                                    int size=dist(number-constrained,groupNo-constrainedNo,nonconstrained);
                                    for(int x=0;x<size;x++){
                                        if(((CheckBox)cv.getChildAt(i).findViewById(R.id.maleCheck)).isChecked()
                                                &&((SeekBar)cv.getChildAt(i).findViewById(R.id.maleBar)).getProgress()>m){
                                            m++;
                                            String temp=males.get(rnd.nextInt(males.size()));
                                            males.remove(temp);
                                            names.remove(temp);
                                            s.add(temp);
                                        }else if(((CheckBox)cv.getChildAt(i).findViewById(R.id.femaleCheck)).isChecked()
                                                &&((SeekBar)cv.getChildAt(i).findViewById(R.id.femaleBar)).getProgress()>f){
                                            f++;
                                            String temp=females.get(rnd.nextInt(females.size()));
                                            females.remove(temp);
                                            names.remove(temp);
                                            s.add(temp);
                                        }else {
                                            String temp=names.get(rnd.nextInt(names.size()));
                                            names.remove(temp);
                                            s.add(temp);
                                        }
                                    }
                                    nonconstrained++;
                                    Collections.shuffle(s);
                                    if(randSwitch.isChecked()){
                                        int hTaker=rnd.nextInt(s.size());
                                        s.set(hTaker,s.get(hTaker)+"⁕");
                                    }
                                    String title=((EditText)cv.getChildAt(i).findViewById(R.id.groupTitle)).getText().toString();
                                    if(title.equals(""))title="Group "+(containers.size()+1);
                                    containers.add(new contentContainer(title,arrayToString(s)));
                                }
                            }
                            break;
                        default:Snackbar.make(v,"Pick randomization option"+rg.getCheckedRadioButtonId(),Snackbar.LENGTH_LONG).show();
                    }
                }catch (Exception e){
                    Snackbar.make(v,"Invalid group setup",Snackbar.LENGTH_LONG).show();
                }
                Collections.shuffle(lists);
                int x=1;
                for(ArrayList<String> s:lists){
                    if(randSwitch.isChecked()){
                        int hTaker=rnd.nextInt(s.size());
                        s.set(hTaker,s.get(hTaker)+"⁕");
                    }
                    containers.add(new contentContainer("Group "+x++,arrayToString(s)));
                }
                adapter.notifyDataSetChanged();
            }
        }

    }
    public String arrayToString(ArrayList<String> strings){
        String temp="";
        for(String o:strings)temp+="• "+o+"\n";
        return temp;
    }
    public static class RandomizerFragment extends Fragment{
        public RandomizerFragment(){

        }
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            // Inflate the layout for this fragment
            final View view=inflater.inflate(R.layout.fragment_randomizer, container, false);
            RecyclerView recyclerView = view.findViewById(R.id.recyclerView);
            final Spinner spinner=view.findViewById(R.id.randSpinner);
            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            recyclerView.setAdapter(adapter);
            final ArrayList<String> names=new ArrayList<>();
            for(Group g:groups)names.add(g.name);
            SpinnerAdapter adapter=new ArrayAdapter<String>(view.getContext(),android.R.layout.simple_spinner_dropdown_item,names);
            spinner.setAdapter(adapter);
            final LinearLayout multiLayout=view.findViewById(R.id.multiLayout);
            final Switch multiSwitch=view.findViewById(R.id.multiSwitch);
            final Context context=view.getContext();
            final ScrollView scroll=view.findViewById(R.id.scroll);
            final RadioGroup rg=view.findViewById(R.id.randGroup);
            final ScrollView scrollView=view.findViewById(R.id.scrollView);
            scrollView.setVisibility(View.GONE);
            multiSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    if(b){
                        spinner.setVisibility(View.GONE);
                        multiLayout.setVisibility(View.VISIBLE);
                        scroll.setVisibility(View.VISIBLE);
                        rg.setVisibility(View.GONE);
                        multiLayout.removeAllViews();
                        scrollView.setVisibility(View.VISIBLE);
                        for(String s:names){
                            CheckBox c=new CheckBox(context);
                            c.setText(s);
                            multiLayout.addView(c);
                        }
                    }else{
                        spinner.setVisibility(View.VISIBLE);
                        multiLayout.setVisibility(View.GONE);
                        scroll.setVisibility(View.GONE);
                        rg.setVisibility(View.VISIBLE);
                        scrollView.setVisibility(View.GONE);
                    }
                }
            });
            final LinearLayout customizerView=view.findViewById(R.id.customizerView);
            rg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup radioGroup, int i) {
                    int id=0;
                    for(int j=0;j<5;j++)if(((RadioButton)radioGroup.getChildAt(j)).isChecked())id=j;
                    if(id!=4) scrollView.setVisibility(View.GONE);
                }
            });
            final EditText editText = view.findViewById(R.id.editText);
            inflater = getLayoutInflater();
            final LayoutInflater finalInflater = inflater;
            editText.addTextChangedListener(new TextWatcher() {
                int groupNo=0;
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    int existingGroups=customizerView.getChildCount();
                    ArrayList<String> females=new ArrayList<>();
                    ArrayList<String> males =new ArrayList<>();
                    if(multiSwitch.isChecked()){
                        try{
                            for(int j=0;j<groups.size();j++){
                                if(((CheckBox)multiLayout.getChildAt(j)).isChecked()){
                                    ArrayList<Student> students=groups.get(j).students;
                                    for(Student s:students) {
                                        if(s.gender==Gender.Female)females.add(s.name);else males.add(s.name);
                                    }
                                }
                            }
                        }catch (Exception e){
                        }
                    }else{
                        try{
                            ArrayList<Student> students=groups.get(spinner.getSelectedItemPosition()).students;
                            for(Student s:students) {
                                if(s.gender==Gender.Female)females.add(s.name);else males.add(s.name);
                            }
                        }catch (Exception e){
                        }
                    }
                    final int m=males.size();
                    final int f=females.size();
                    final int n=m+f;
                    try{
                        if(!charSequence.toString().equals(""))groupNo=Integer.parseInt(charSequence.toString());
                        else groupNo=existingGroups;
                    }catch (Exception e){

                    }
                    if(groupNo>existingGroups){
                        for(int x=0;x<groupNo-existingGroups;x++){
                            final CardView cv=(CardView) finalInflater.inflate(R.layout.custom_group,null);
                            final CheckBox numberCheck=cv.findViewById(R.id.numberCheck);
                            final CheckBox maleCheck=cv.findViewById(R.id.maleCheck);
                            final CheckBox femaleCheck=cv.findViewById(R.id.femaleCheck);
                            final SeekBar femaleBar=cv.findViewById(R.id.femaleBar);
                            final SeekBar maleBar=cv.findViewById(R.id.maleBar);
                            final EditText groupSize=cv.findViewById(R.id.groupSize);
                            groupSize.addTextChangedListener(new TextWatcher() {
                                @Override
                                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                                }

                                @Override
                                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                                    updateDisplay(n,m,f);
                                    if(!numberCheck.isChecked())numberCheck.setChecked(true);
                                }

                                @Override
                                public void afterTextChanged(Editable editable) {

                                }
                            });
                            maleBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                                @Override
                                public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                                    updateDisplay(n,m,f);
                                    if(b&&!maleCheck.isChecked())maleCheck.setChecked(true);
                                }

                                @Override
                                public void onStartTrackingTouch(SeekBar seekBar) {

                                }

                                @Override
                                public void onStopTrackingTouch(SeekBar seekBar) {

                                }
                            });
                            femaleBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                                @Override
                                public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                                    updateDisplay(n,m,f);
                                    if(b&&!femaleCheck.isChecked())femaleCheck.setChecked(true);
                                }

                                @Override
                                public void onStartTrackingTouch(SeekBar seekBar) {

                                }

                                @Override
                                public void onStopTrackingTouch(SeekBar seekBar) {

                                }
                            });
                            customizerView.addView(cv);
                        }
                    }else if(existingGroups>groupNo){
                        for(int x=0;x<existingGroups-groupNo;x++)
                            customizerView.removeView(customizerView.getChildAt(customizerView.getChildCount()-1));
                    }
                }

                @Override
                public void afterTextChanged(Editable editable) {

                }
                public void updateDisplay(int n, int m, int f){
                    int reserved=0;
                    int rM=0;
                    int rF=0;
                    for(int i=0;i<groupNo;i++){
                        try{
                            reserved+=Integer.parseInt(((EditText)customizerView.getChildAt(i).
                                    findViewById(R.id.groupSize)).getText().toString());
                            rM+=((SeekBar)customizerView.getChildAt(i).
                                    findViewById(R.id.maleBar)).getProgress();
                            rF+=((SeekBar)customizerView.getChildAt(i).
                                    findViewById(R.id.femaleBar)).getProgress();
                        }catch (Exception e){}
                    }
                    for(int i=0;i<groupNo;i++){
                        TextView tv=(TextView)customizerView.getChildAt(i).findViewById(R.id.groupBound);
                        SeekBar maleBar=(SeekBar)customizerView.getChildAt(i).findViewById(R.id.maleBar);
                        SeekBar femaleBar=(SeekBar)customizerView.getChildAt(i).findViewById(R.id.femaleBar);
                        TextView maleDisp=customizerView.getChildAt(i).findViewById(R.id.maleDisp);
                        TextView femaleDisp=customizerView.getChildAt(i).findViewById(R.id.femaleDisp);
                        if(n-reserved<0){
                            tv.setTextColor(android.graphics.Color.rgb(200, 0, 0));
                            tv.setText("Invalid");
                        }else if(n==reserved){
                            tv.setTextColor(android.graphics.Color.rgb(0, 200, 0));
                            tv.setText("OK");
                        }else{
                            tv.setTextColor(android.graphics.Color.rgb(115, 115, 115));
                            tv.setText((n-reserved)+" left");
                        }
                        int groupSize=0;
                        try{
                            groupSize=Integer.parseInt(((EditText)customizerView.getChildAt(i).
                                    findViewById(R.id.groupSize)).getText().toString());
                        }catch (Exception e){}
                        maleBar.setMax(Math.min(m-rM+maleBar.getProgress(),groupSize-femaleBar.getProgress()));
                        femaleBar.setMax(Math.min(f-rF+femaleBar.getProgress(),groupSize-maleBar.getProgress()));
                        maleDisp.setText(maleBar.getProgress()+"/"+m);
                        femaleDisp.setText(femaleBar.getProgress()+"/"+f);
                    }
                }
            });
            return view;
        }
    }
    public static class ListFragment extends Fragment{
        public ListFragment(){

        }
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            // Inflate the layout for this fragment
            View view=inflater.inflate(R.layout.fragment_list, container, false);
            updateFromFragment(view,inflater);
            return view;
        }
    }
//    public class contentContainer{
//        public String title, content;
//        public contentContainer(String t,String c){
//            title=t;content=c;
//        }
//
//    }
//    public class CardAdapter extends RecyclerView.Adapter<Holder>{
//        private Context context;
//        private ArrayList<contentContainer> cards;
//        public CardAdapter(Context context, ArrayList<contentContainer> cards) {
//            this.context = context;
//            this.cards = cards;
//        }
//        @Override
//        public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
//            View view = LayoutInflater.from(context).inflate(R.layout.item_row,parent, false);
//            return new Holder(view);
//        }
//
//        @Override
//        public void onBindViewHolder(Holder holder, int position) {
//            contentContainer card= cards.get(position);
//            holder.setDetails(card);
//        }
//
//        @Override
//        public int getItemCount() {
//            return cards.size();
//        }
//    }
//    public class Holder extends RecyclerView.ViewHolder{
//        private TextView txtName, txtText;
//        public Holder(View itemView) {
//            super(itemView);
//            txtName = itemView.findViewById(R.id.txtName);
//            txtText = itemView.findViewById(R.id.txtText);
//        }
//        public void setDetails(contentContainer container) {
//            txtName.setText(container.title);
//            txtText.setText(container.content);
//        }
//    }
    public static class contentContainer{
        public String title, content;
        public View view;
        boolean containsView=false;
        boolean hasTitle=true;
        public contentContainer(String t,String c){
            title=t;content=c;
        }
        public contentContainer(String t, String c, View v){
            title=t;content=c;view=v;
            if(title.equals(""))hasTitle=false;
            containsView=true;
        }
    }
    public static class CardAdapter extends RecyclerView.Adapter<Holder>{
        private Context context;
        private ArrayList<contentContainer> cards;
        public CardAdapter(Context context, ArrayList<contentContainer> cards) {
            this.context = context;
            this.cards = cards;
        }
        @Override
        public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_row,parent, false);
            return new Holder(view);
        }

        @Override
        public void onBindViewHolder(Holder holder, int position) {
            contentContainer card= cards.get(position);
            holder.setDetails(card);
        }

        @Override
        public int getItemCount() {
            return cards.size();
        }

        @Override
        public int getItemViewType(int position){
            //if(!cards.get(position).title.equals(""))return 42;
            return 0;
        }
    }
    public static class Holder extends RecyclerView.ViewHolder{
        private TextView txtName, txtText;
        private LinearLayout layout;
        public Holder(View itemView) {
            super(itemView);
            txtName = itemView.findViewById(R.id.txtName);
            txtText = itemView.findViewById(R.id.txtText);
            layout = itemView.findViewById(R.id.txtView);
        }
        public void setDetails(contentContainer container) {
            layout.removeAllViews();
            txtName.setText(container.title);
            txtText.setText(container.content);
            if(container.containsView){
                if(container.view.getParent()==null)layout.addView(container.view);
                txtText.setVisibility(View.GONE);
                if(!container.hasTitle)txtName.setVisibility(View.GONE);
                container.view.setFocusable(true);
            }
        }
    }
    public class Group{
        String name;
        ArrayList<Student> students=new ArrayList<>();
        int id;
        public Group(String name){
            this.name=name;
        }
        public Group(String name, ArrayList students){
            groupIDs=prefs.getInt("groupIDs",0);
            this.name=name;
            this.students=students;
            id=groupIDs++;
            prefs.edit().putInt("groupIDs",groupIDs).apply();
        }
        int getSize(){
            return students.size();
        }
        void addStudent(String name, Gender gender){
            students.add(new Student(name, gender));
        }
        public Group clone(){
            return new Group(name,(ArrayList) students.clone());
        }
        public Group setName(String name){
            this.name=name;
            return this;
        }
    }
    public static class Student{
        Gender gender;
        String name;
        public Student(String name, Gender gender){
            this.gender=gender;
            this.name=name;
        }
    }
    public void updateLists(){
        updateFromFragment(findViewById(R.id.groupList),getLayoutInflater());
// LinearLayout groupList=findViewById(R.id.groupList);
//        groupList.removeAllViews();
//        context=this;
//        for(final Group g:groups){
//            final LayoutInflater inflater=getLayoutInflater();
//            CardView card=(CardView) inflater.inflate(R.layout.group_card,null);
//            //TextView name= card.findViewById(R.id.groupName);
//            LinearLayout ll= (LinearLayout) (card.getChildAt(0));
//            TextView name= (TextView) ll.getChildAt(0);
//            final TextView number = card.findViewById(R.id.groupNo);
//            ImageView add = card.findViewById(R.id.addButton);
//            ImageView del = card.findViewById(R.id.deleteButton);
//            ImageView copy = card.findViewById(R.id.copyButton);
//            ImageView edit = card.findViewById(R.id.editButton);
//            name.setText(g.name);
//            int size=g.getSize();
//            int males=0;
//            for(Student s:g.students)if(s.gender==Gender.Male)males++;
//            number.setText(size+" ("+(size-males)+", "+males+")");
//            final int i=groups.indexOf(g);
//            add.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//
//                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
//                    // Get the layout inflater
//                    LayoutInflater inflater = getLayoutInflater();
//                    final View v=inflater.inflate(R.layout.student_fragment,null);
//                    // Inflate and set the layout for the dialog
//                    // Pass null as the parent view because its going in the dialog layout
//                    builder.setView(v);
//                    builder.setPositiveButton("Add", new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialog, int id) {
//                            EditText et = v.findViewById(R.id.studentName);
//                            RadioButton female = v.findViewById(R.id.radio1);
//                            Gender gender;
//                            if (female.isChecked()) gender = Gender.Female;
//                            else gender = Gender.Male;
//                            groups.get(i).addStudent(et.getText().toString(), gender);
//                            updateLists();
//                        }
//                    });
//                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
//                        public void onClick(DialogInterface dialog, int id) {
//
//                        }
//                    });// Add action buttons
//                    builder.show();
//                }
//            });
//            del.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
//                    builder.setMessage("Are you sure you want to delete this group?")
//                            .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
//                                public void onClick(DialogInterface dialog, int id) {
//                                    groups.remove(i);
//                                    updateLists();
//                                }
//                            })
//                            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
//                                public void onClick(DialogInterface dialog, int id) {
//                                    // User cancelled the dialog
//                                }
//                            });
//                    builder.show();
//
//                }
//            });
//            copy.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    groups.add(g.clone().setName(g.name+" (Copy)"));
//                    updateLists();
//                }
//            });
//            edit.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    Group tmpGroup=g.clone();
//                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
//                    RecyclerView rv=new RecyclerView(context);
//                    EditText editText=new EditText(context);
//                    editText.setText(tmpGroup.name);
//                    for(Student s:tmpGroup.students){
//                        CardView c=new CardView(context);
//                        LinearLayout ll= (LinearLayout) inflater.inflate(R.layout.student_fragment,c);
//                        ImageView sDel=ll.findViewById(R.id.studentDelete);
//                        sDel.setVisibility(View.VISIBLE);
//                        ((EditText)ll.findViewById(R.id.studentName)).setText(s.name);
//                        if(s.gender==Gender.Male)((RadioButton)ll.findViewById(R.id.radio2)).setChecked(true);
//                    }
//                    builder.setPositiveButton("Edit", new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialog, int id) {
//
//                        }
//                    });
//                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
//                        public void onClick(DialogInterface dialog, int id) {
//
//                        }
//                    });
//                    builder.show();
//                }
//            });
//            groupList.addView(card);
//        }
//        saveGroups();
    }
    public static void updateFromFragment(final View v, final LayoutInflater inflater){
        LinearLayout groupList=v.findViewById(R.id.groupList);
        groupList.removeAllViews();
        for(final Group g:groups){
            //LayoutInflater inflater=v.getLayoutInflater();
            CardView card=(CardView) inflater.inflate(R.layout.group_card,null);
            //TextView name= card.findViewById(R.id.groupName);
            LinearLayout ll= (LinearLayout) (card.getChildAt(0));
            TextView name= (TextView) ll.getChildAt(0);
            final TextView number = card.findViewById(R.id.groupNo);
            ImageView add = card.findViewById(R.id.addButton);
            ImageView del = card.findViewById(R.id.deleteButton);
            ImageView copy = card.findViewById(R.id.copyButton);
            final ImageView export = card.findViewById(R.id.exportButton);
            final ImageView edit = card.findViewById(R.id.editButton);
            name.setText(g.name);
            int size=g.getSize();
            int males=0;
            for(Student s:g.students)if(s.gender==Gender.Male)males++;
            number.setText(size+" ("+(size-males)+", "+males+")");
            final int i=groups.indexOf(g);
            add.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                    // Get the layout inflater
                    final View ve=inflater.inflate(R.layout.student_fragment,null);
                    // Inflate and set the layout for the dialog
                    // Pass null as the parent view because its going in the dialog layout
                    builder.setView(ve);
                    builder.setPositiveButton("Add", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            EditText et = ve.findViewById(R.id.studentName);
                            RadioButton female = ve.findViewById(R.id.radio1);
                            Gender gender;
                            if (female.isChecked()) gender = Gender.Female;
                            else gender = Gender.Male;
                            groups.get(i).addStudent(et.getText().toString(), gender);
                            updateFromFragment(v,inflater);
                        }
                    });
                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {

                        }
                    });
                    builder.show();
                }
            });
            del.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                    builder.setMessage("Are you sure you want to delete this group?")
                            .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    groups.remove(i);
                                    updateFromFragment(v,inflater);
                                }
                            })
                            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    // User cancelled the dialog
                                }
                            });
                    builder.show();

                }
            });
            copy.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    groups.add(g.clone().setName(g.name+" (Copy)"));
                    updateFromFragment(v,inflater);
                }
            });
            export.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    StringBuilder s=new StringBuilder();
                    for(Student x:g.students){
                        s.append(x.name);
                        s.append(x.gender==Gender.Male?", M\n":", F\n");
                    }
                    ClipData clip = ClipData.newPlainText("Group", s.toString().trim());
                    clipboard.setPrimaryClip(clip);
                    Snackbar.make(view,"Group exported and copied to clipboard!",Snackbar.LENGTH_LONG).show();
                }
            });
            export.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    StringBuilder s=new StringBuilder();
                    for(Student x:g.students) s.append(x.name).append("\n");
                    ClipData clip = ClipData.newPlainText("Names", s.toString().trim());
                    clipboard.setPrimaryClip(clip);
                    Snackbar.make(view,"Names copied to clipboard!",Snackbar.LENGTH_LONG).show();
                    return true;
                }
            });
            edit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Group tmpGroup=g.clone();
                    AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                    //final ArrayList<contentContainer> contentContainers=new ArrayList<>();
                    final LinearLayout rv=new LinearLayout(v.getContext());
                    rv.setOrientation(LinearLayout.VERTICAL);
                    final EditText editText=new EditText(v.getContext());
                    editText.setText(g.name);
                    editText.setFocusable(true);
                    editText.setFocusableInTouchMode(true);
                    TextView tv=new TextView(v.getContext());
                    tv.setText("Group Name:");
                    tv.setTextSize(21);
                    tv.setTextColor(Color.parseColor("#000000"));
                    tv.setGravity(Gravity.CENTER);
                    LinearLayout horizontalLayout=new LinearLayout(v.getContext());
                    horizontalLayout.setOrientation(LinearLayout.HORIZONTAL);
                    horizontalLayout.addView(tv, -2,-1);
                    horizontalLayout.addView(editText,-1,-2);
                    horizontalLayout.setPadding(15,15,15,15);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        horizontalLayout.setElevation(5);
                    }
                    rv.addView(horizontalLayout,-1,-2);
                    for(Student s:tmpGroup.students){
                        final LinearLayout ll= (LinearLayout) inflater.inflate(R.layout.student_fragment,null);
                        ImageView sDel=ll.findViewById(R.id.studentDelete);
                        sDel.setVisibility(View.VISIBLE);
                        ((EditText)ll.findViewById(R.id.studentName)).setText(s.name);
                        ((EditText)ll.findViewById(R.id.studentName)).setFocusable(true);
                        if(s.gender==Gender.Male)((RadioButton)ll.findViewById(R.id.radio2)).setChecked(true);
                        final contentContainer cc=new contentContainer("","",ll);
                        sDel.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                rv.removeView(ll);
                                for(int i=1;i<rv.getChildCount();i++){
                                    rv.getChildAt(i).setBackgroundColor(Color.parseColor("#ffffff"));
                                    //rv.getChildAt(i).setElevation(5);
                                }
                                for(int i=1;i<rv.getChildCount();i++)if(i%2==1){
                                    rv.getChildAt(i).setBackgroundColor(Color.parseColor("#f1f1f1"));
                                    //rv.getChildAt(i).setElevation(5);
                                }
                            }
                        });
                        ll.setPadding(15,15,15,15);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            ll.setElevation(5);
                        }
                        if(tmpGroup.students.indexOf(s)%2==0){
                            ll.setBackgroundColor(Color.parseColor("#f1f1f1"));
                        }
                        rv.addView(ll,-1,-2);
                    }
                    ScrollView sv=new ScrollView(v.getContext());
                    sv.addView(rv);
                    builder.setView(sv);
                    builder.setPositiveButton("Edit", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            g.name=editText.getText().toString();
                            ArrayList<Student> ss=new ArrayList<>();
                            for(int i=1;i<rv.getChildCount();i++){
                                LinearLayout cc= (LinearLayout) rv.getChildAt(i);
                                String name=((EditText)((LinearLayout)cc.getChildAt(0)).getChildAt(0)).getText().toString();
                                Boolean isFemale=((RadioButton)((LinearLayout)cc.getChildAt(1)).getChildAt(0)).isChecked();
                                ss.add(new Student(name,isFemale?Gender.Female:Gender.Male));
                            }
                            g.students=ss;
                            updateFromFragment(v,inflater);
                        }
                    });
                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {


                        }
                    });
                    builder.show().getWindow().setSoftInputMode (WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);;
                }
            });
            groupList.addView(card);
        }
        saveGroups();
    }
    public void addGroup(){
        final EditText txtName = new EditText(this);
        new AlertDialog.Builder(this)
                .setTitle("Add group")
                .setMessage("Enter group name")
                .setView(txtName)
                .setPositiveButton("Add", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        String name = txtName.getText().toString();
                        groups.add(new Group(name));
                        updateLists();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    }
                })
                .setNeutralButton("Import", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        ClipData clipData = clipboard.getPrimaryClip();
                        importGroup(txtName.getText().toString(),clipData.getItemAt(0).getText().toString());
                    }
                })
                .show();
    }
    public static void saveGroups(){
//        ArrayList<String> names=new ArrayList<>();
//        //Set<String> names;
//        SharedPreferences.Editor editor=prefs.edit();
//        for(Group g:groups){
//            names.add(g.name+"_"+g.id);
//            editor.putString()
//        }
//        Set<String> set=new HashSet(names);
//        editor.putStringSet("groupNames",set);
        String grs=gson.toJson(groups);
        prefs.edit().putString("groups",grs).apply();
    }
    public void getGroups(){
        String grs=prefs.getString("groups","");
        if(!grs.equals("")){
            Type arrayType=new TypeToken<ArrayList<Group>>(){}.getType();
            groups=gson.fromJson(grs,arrayType);
        }
    }
//    public void createMenu(final Group g){
//        AlertDialog.Builder builder = new AlertDialog.Builder(this);
//        builder.setTitle("");
//        String[] options={"Copy", "Edit", "Delete"};
//        builder.setItems(options, new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialogInterface, int i) {
//                switch (i){
//                    case 0:
//                        groups.add(g.setName(g.name+" (Copy)"));
//                        updateLists();
//                        break;
//                    case 1:
//                        break;
//                    case 2:
//                        break;
//                }
//            }
//        });
//        AlertDialog dialog = builder.create();
//        dialog.show();
//    }
//    public static float dipToPixels(Context context, float dipValue){
//        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
//        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,  dipValue, metrics);
//    }
    public int dist(int size, int number, int index){
        return size/number+(index<size%number?1:0);
    }
    public void toggleCustomizer(View view){
        ScrollView scrollView=findViewById(R.id.scrollView);
        if(scrollView.getVisibility()==View.VISIBLE) scrollView.setVisibility(View.GONE);
        else scrollView.setVisibility(View.VISIBLE);
    }
    public boolean copy(View view){
        StringBuilder s= new StringBuilder();
        for(contentContainer c:containers){
            s.append(c.title).append("\n").append("_____________\n");
            s.append(c.content).append("\n\n");
        }
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Groups", s.toString().trim());
        clipboard.setPrimaryClip(clip);
        Snackbar.make(view,"Copied to clipboard!",Snackbar.LENGTH_LONG).show();
        return true;
    }
    public void importGroup(String name, String s){
        String[] lines =s.split("\n");
        Group g=new Group(name);
        for(String x:lines){
            if(x.contains(",")){
                String[] split=x.split(",");
                Gender gender=split[1].trim().equals("M")?Gender.Male:Gender.Female;
                g.addStudent(split[0].trim(),gender);
            }
            else g.addStudent(x.trim(),Gender.Female);
        }
        groups.add(g);
        updateLists();
    }
    public void modeSwitch(View view){

    }
}
