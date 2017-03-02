package hr.riteh.moreno.registracije;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by moren on 07-Jan-17.
 */

public class CustomAdapter extends BaseAdapter{

    private Activity activity;
    private ArrayList<Cars> data;
    private static LayoutInflater inflater = null;


    public CustomAdapter(Activity activity, ArrayList<Cars> data){
        this.activity = activity;
        this.data = data;

        inflater = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount(){
        return data.size();
    }

    @Override
    public Object getItem(int location){
        return data.get(location);
    }

    @Override
    public long getItemId(int position){
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        if(inflater == null){
            inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }
        if (convertView == null){
            convertView = inflater.inflate(R.layout.row, null);
        }

        TextView reg = (TextView) convertView.findViewById(R.id.registracija);
        TextView tip = (TextView) convertView.findViewById(R.id.tip);
        TextView sasija = (TextView) convertView.findViewById(R.id.sasija);
        TextView kuca = (TextView) convertView.findViewById(R.id.kuca);
        TextView polica = (TextView) convertView.findViewById(R.id.polica);

        Cars car = data.get(position);

        reg.setText(car.getReg());
        tip.setText(car.getTip());
        sasija.setText(car.getSasija());
        kuca.setText(car.getKuca());
        polica.setText(car.getPolica());

        return convertView;
    }

}
