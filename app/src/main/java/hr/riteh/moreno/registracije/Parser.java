package hr.riteh.moreno.registracije;

import android.app.Activity;
import android.os.AsyncTask;
import android.widget.ListView;
import android.widget.Toast;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.util.ArrayList;


/**
 * Created by moren on 06-Jan-17.
 */

public class Parser extends AsyncTask<String, Void, ArrayList<Cars>> {
    private Activity activity;
    //private ArrayList<Cars> dataList = new ArrayList<>();
    private Cars car;
    private boolean kod = false;

    public Parser(Activity activity){
        this.activity = activity;
    }


    @Override
    protected ArrayList<Cars> doInBackground(String... data) {
        ArrayList<Cars> dataList = new ArrayList<>();

        for (String html : data){
            car = new Cars();

            Document doc = Jsoup.parse(html);
            Elements elements = doc.select("div.sadrzaj_middle_ strong");

            if(elements.size() < 4){
                if(elements.get(1).toString().contains("krivi")){
                    kod = true;
                }
                elements.empty();
            }

            if ((elements.get(0).hasText())) {
                for (int i=0; i<=elements.size();i++) {
                    //dataList.add(e.text());
                    if (i==1) car.setReg(elements.get(i).text());
                    else if (i==2) car.setTip(elements.get(i).text());
                    else if (i==3) car.setSasija(elements.get(i).text());
                    else if (i==5) car.setKuca(elements.get(i).text());
                    else if (i==6) car.setPolica(elements.get(i).text());
                }
            }
            if (car.getTip() != null)
                dataList.add(car);
        }
    return dataList;
    }

    @Override
    protected void onPostExecute(ArrayList<Cars> list) {
        super.onPostExecute(list);
        if (kod){
            Toast toast = Toast.makeText(activity.getApplicationContext(), "Upisali ste krivi kod sa slike!", Toast.LENGTH_SHORT);
            toast.show();
            activity.finish();
        } else {
            ListView listView = (ListView) activity.findViewById(R.id.list);
            CustomAdapter adapter = new CustomAdapter(activity, list);
            if(adapter.getCount() != 0){
                listView.setAdapter(adapter);
                adapter.notifyDataSetChanged();
            } else {
                Toast toast = Toast.makeText(activity.getApplicationContext(), "Nije pronađeno niti jedno vozilo!", Toast.LENGTH_SHORT);
                toast.show();
                activity.finish();
            }
        }
    }
}
