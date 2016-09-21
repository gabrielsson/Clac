package cl.sidan.clac.adapters;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LevelListDrawable;
import android.os.AsyncTask;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cl.sidan.clac.R;
import cl.sidan.clac.access.interfaces.Entry;
import cl.sidan.clac.access.interfaces.User;


public class AdapterEntries extends ArrayAdapter<Entry> implements Filterable {

    private static final DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

    private final List<Entry> items;

    Context context;
    float fontsize;

    private boolean filteredResults = false;

    public AdapterEntries(Context context, int resource, List<Entry> objects, float fontsize) {
        super(context, resource, objects);
        this.context = context;
        this.fontsize = fontsize;

        this.items = objects;
    }

    private static class ViewHolder {
        TextView txtEntry = null;
        TextView txtSignatureLine = null;
        GridLayout enhetGrid = null;
    }

    @Override
    public final void sort(Comparator<? super Entry> comparator) {
        Comparator<Entry> entryComparator = new Comparator<Entry>() {
            @Override
            public int compare(Entry entry, Entry entry2) {
                return entry2.getId() - entry.getId();
            }
        };

        super.sort(entryComparator);

    }

    @Override
    public final Entry getItem(int position) {
        if (position < getCount()) {
            return super.getItem(position);
        }
        return null;
    }

    @Override
    public final View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        Entry entry = getItem(position);
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Activity.LAYOUT_INFLATER_SERVICE);

        if (convertView == null) {
            holder = new ViewHolder();
            convertView = inflater.inflate(R.layout.entry, null);
            holder.txtEntry = (TextView) convertView.findViewById(R.id.entry);
            holder.txtSignatureLine = (TextView) convertView.findViewById(R.id.signature_line);
            holder.enhetGrid = (GridLayout) convertView.findViewById(R.id.enhetGrid);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        convertView.setTag(holder);

        if (entry == null) {
            return convertView;
        }

        holder.txtEntry.setTextSize(fontsize);
        holder.txtEntry.setText(linkifyHtml(entry.getMessage().trim().replaceAll("\\n", "<br />"), new URLImageParser(holder.txtEntry, context)));
        holder.txtEntry.setMovementMethod(LinkMovementMethod.getInstance());

        Date date = entry.getDateTime();
        String dayOfWeek = getDayOfWeekString(date);
        holder.txtSignatureLine.setTextSize(fontsize);
        List<User> kumpanLista = entry.getKumpaner();
        String kumpanString = "";
        for( int i = 0; i < kumpanLista.size(); i++) {
            kumpanString += kumpanLista.get(i).getSignature() + ",";
        }
        if( !kumpanString.isEmpty() ) {
            kumpanString = " | " + kumpanString.substring(0, kumpanString.length()-1);
        }

        String signatureLineText = String.format(Locale.getDefault(),
                "%s | %s (%s) %s | %d", // #68 | 2015-11-10 (tis) #38,#71 | 42
                entry.getSignature(), format.format(date),
                dayOfWeek, kumpanString, entry.getLikes());
        holder.txtSignatureLine.setText(signatureLineText);

        int color;
        switch (entry.getStatus()) {
            case 1: //politics
                color = getThemeColor(R.attr.politics);
                holder.txtEntry.setTextColor(context.getResources().getColor(color));
                holder.txtSignatureLine.setTextColor(context.getResources().getColor(color));
                break;
            case 2: //#27 - complaints
                color = getThemeColor(R.attr.complaints27);
                holder.txtEntry.setTextColor(context.getResources().getColor(color));
                holder.txtSignatureLine.setTextColor(context.getResources().getColor(color));
                break;
            case 3: //#44 - noshow
                color = getThemeColor(R.attr.noshow44);
                holder.txtEntry.setTextColor(context.getResources().getColor(color));
                holder.txtSignatureLine.setTextColor(context.getResources().getColor(color));
                break;
            case 4: //#31 vs #45 - nerd #FF8000
                color = getThemeColor(R.attr.nerd31vs45);
                holder.txtEntry.setTextColor(context.getResources().getColor(color));
                holder.txtSignatureLine.setTextColor(context.getResources().getColor(color));
                break;
            case 5: //NSFW
                color = getThemeColor(R.attr.nsfw);
                holder.txtEntry.setTextColor(context.getResources().getColor(color));
                holder.txtSignatureLine.setTextColor(context.getResources().getColor(color));
                holder.txtEntry.setText(String.format("NSFW!! %s", holder.txtEntry.getText()));
                break;
            case 0: //normal
            default:
                color = getThemeColor(R.attr.normalentry);
                holder.txtEntry.setTextColor(context.getResources().getColor(color));
                holder.txtSignatureLine.setTextColor(context.getResources().getColor(color));
                break;
        }

        ImageView arrayOfImages[] = new ImageView[entry.getEnheter()];

        holder.enhetGrid.removeAllViewsInLayout();
        for (int i = 0; i < entry.getEnheter(); i++) {
            ImageView image = (ImageView) inflater.inflate(R.layout.olsug_image, null);

            image.setDrawingCacheEnabled(true);
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.WRAP_CONTENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT);
            if (i != 0) {
                params.addRule(RelativeLayout.RIGHT_OF,
                        arrayOfImages[i - 1].getId());
            }
            arrayOfImages[i] = image;

            holder.enhetGrid.addView(image, params);
        }

        return convertView;
    }

    private int getThemeColor(int attr) {
        TypedValue attrValue = new TypedValue();
        context.getTheme().resolveAttribute(attr, attrValue, true);
        return attrValue.resourceId;
    }

    private static String getDayOfWeekString(Date date) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        int dayOfWeek = c.get(Calendar.DAY_OF_WEEK);

        switch (dayOfWeek) {
            case 1:
                return "sön";
            case 2:
                return "mån";
            case 3:
                return "tis";
            case 4:
                return "ons";
            case 5:
                return "tor";
            case 6:
                return "fre";
            case 7:
                return "lör";
            default:
                return "SKOTTDAGEN?????";
        }
    }

    public final Spannable linkifyHtml(String html, URLImageParser urlIP) {
        html = replaceURIs(html);

        /* Convert the html */
        Spanned text = Html.fromHtml(html, urlIP, null);

        return new SpannableString(text);
    }

    public static String replaceURIs(String html) {
        /**
         *  We match anything that even remotely look like a url, convert it to a propper url with
         *  <a></a>-tags. Then we delete the <a></a>-tags if the url is already enclosed in them.
         *  We also add the full url to local images that has been uploaded to /inmailat/.
         **/
        // convert links that does not start with "=" to real <a href='link'>link</a>
        // (?<!) is used for negative look-behind; the matching test cannot start with this string,
        // but it is not part of the regexp (not captured). Check https://regex101.com/ for a
        // decent online regexp builder.

        // if the link is in the form "inmailat/...jpg" prepend full url.
        html = Pattern.compile("(?<![/>])(inmailat/.*\\.)([a-zA-Z]{2,3})", Pattern.CASE_INSENSITIVE)
                .matcher(html).replaceAll("http://sidan.cl/$1$2");

        Matcher m = Pattern.compile("((?:https?://|ftps?://|www\\d{0,3}\\.)(?:[^\\s()<>]+|\\(([^\\s()<>]+|(\\([^\\s()<>]+\\)))*\\))+(?:\\(([^\\s()<>]+|(\\([^\\s()<>]+\\)))*\\)|[^\\s`!()\\[\\]{};:'\".,<>?«»“”‘’]))", Pattern.CASE_INSENSITIVE)
                .matcher(html);

        // If we find an URI, split it and fix the different parts recursively; not really the Java
        // way, but fuck it, I don't care anymore.
        if (m.find()) {
            String head = html.substring(0, m.start()),
                    url = m.group(),
                    tail = html.substring(m.end(), html.length());

            if (head.endsWith("=")) {
                return head + "\'" + url + "\'" + replaceURIs(tail);
            } else if (!head.matches(".*=['\"]")) {
                return head + "<a href='" + url + "'>" + url + "</a>" + replaceURIs(tail);
            }
        }

        return html;
    }


    public final class URLImageParser implements Html.ImageGetter {
        ArrayList<String> localImagesURLArray  = new ArrayList<>();
        ArrayList<Integer> localImagesResArray = new ArrayList<>();
        String[] localImagesURLSources = {
                "minipics/checkbox.gif",
                "minipics/checkbox_unchecked.gif",
                "minipics/Giggling.gif",
                "minipics/knulla.gif",
                "minipics/knulltna.gif",
                "minipics/Pivo.gif",
                "minipics/Smiley0.gif",
                "minipics/Smiley1.gif",
                "minipics/Smiley2.gif",
                "minipics/Smiley3.gif",
                "minipics/Smiley4.gif",
                "minipics/Smiley5.gif",
                "minipics/Smiley6.gif",
                "minipics/Smiley7.gif",
                "minipics/Smiley8.gif",
                "minipics/Smiley9.gif",
                "minipics/Smiley10.gif",
                "minipics/Smiley11.gif",
                "minipics/Smiley12.gif",
                "minipics/Smiley13.gif",
                "minipics/svina.gif",
                "minipics/supa.gif",
                "minipics/proppeller.gif"
        };
        Integer[]  localImagesResources = {
                R.drawable.checkbox,
                R.drawable.checkbox_unchecked,
                R.drawable.giggling,
                R.drawable.knulla,
                R.drawable.knulltna,
                R.drawable.pivo,
                R.drawable.smiley0,
                R.drawable.smiley1,
                R.drawable.smiley2,
                R.drawable.smiley3,
                R.drawable.smiley4,
                R.drawable.smiley5,
                R.drawable.smiley6,
                R.drawable.smiley7,
                R.drawable.smiley8,
                R.drawable.smiley9,
                R.drawable.smiley10,
                R.drawable.smiley11,
                R.drawable.smiley12,
                R.drawable.smiley13,
                R.drawable.svina,
                R.drawable.olsug_32,
                R.drawable.proppeller
        };
        Context c;
        TextView container;

        public URLImageParser(TextView t, Context c) {
            this.c = c;
            container = t;
            localImagesURLArray.addAll(Arrays.asList(localImagesURLSources));
            localImagesResArray.addAll(Arrays.asList(localImagesResources));
        }

        public final Drawable getDrawable(String source) {
            Log.d("ImageDrawer", "Trying to get image with source " + source);
            int localImage = localImagesURLArray.indexOf(source);
            Drawable localOrEmpty;
            LevelListDrawable d = new LevelListDrawable();
            if( localImage >= 0 ) {
                localOrEmpty = c.getResources().getDrawable(localImagesResArray.get(localImage), c.getTheme());
            } else {
                localOrEmpty = c.getResources().getDrawable(R.drawable.proppeller, c.getTheme());
            }
            d.addLevel(0, 0, localOrEmpty);
            int width = localOrEmpty != null ? localOrEmpty.getIntrinsicWidth() : 0;
            int height = localOrEmpty != null ? localOrEmpty.getIntrinsicHeight() : 0;
            d.setBounds(0, 0, width, height);

            if( localImage < 0 ) {
                if (null == source) {
                    return null;
                } else if (source.startsWith("inmailat")) {
                    new LoadImage(container).execute("http://chalmerslosers.com/" + source, d);
                } else {
                    new LoadImage(container).execute(source, d);
                }
            }

            return d;
        }

        private class LoadImage extends AsyncTask<Object, Void, Bitmap> {

            private LevelListDrawable mDrawable = null;
            private TextView mTv;

            LoadImage(TextView mTv) {
                this.mTv = mTv;
            }

            @Override
            protected final Bitmap doInBackground(Object... params) {
                String source = (String) params[0];
                mDrawable = (LevelListDrawable) params[1];
                try {
                    InputStream is = new URL(source).openStream();
                    return BitmapFactory.decodeStream(is);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected final void onPostExecute(Bitmap bitmap) {
                if (bitmap != null) {
                    BitmapDrawable d = new BitmapDrawable(context.getResources(), bitmap);
                    mDrawable.addLevel(1, 1, d);
                    mDrawable.setBounds(0, 0, bitmap.getWidth(), bitmap.getHeight());
                    mDrawable.setLevel(1);

                    // i don't know yet a better way to refresh TextView
                    // mTv.invalidate() doesn't work as expected
                    CharSequence t = mTv.getText();
                    mTv.setText(t);
                }
            }
        }
    }


    public boolean isFilteredResults(){
        return filteredResults;
    }
    public void setFilteredResults(boolean filteredResults){
        this.filteredResults = filteredResults;
    }

    @Override
    public Filter getFilter() {
        return conversationFilter;
    }

    private Filter conversationFilter = new Filter(){
        private static final String HEMLIS_REGEX = "^<small>hemlis Till .*:</small><br>.*";
        private static final String HEMLIS_REGEX_TRIM = ".*?<br>";

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            Log.d("Entry filter", "performFiltering("+constraint.toString()+")");
            String filterSeq = constraint.toString().toLowerCase();

            FilterResults result = new FilterResults();
            Log.d("Entry filter", "items size: "+items.size());
            if( filterSeq.isEmpty() ){
                result.values = items;
                result.count = items.size();
                setFilteredResults(false);
                return result;
            }

            String[] parts = filterSeq.split(":");
            Log.d("Entry filter", parts[0] + " & " + parts[1]);

            List<Entry> retList = new ArrayList<>();

            int betweenCount = 0; //TODO: Show number of entries hidden
            String lastTargetedSig = null;
            for( Entry e : items ){
                String sig = e.getSignature().toLowerCase();

                if( !parts[0].equals(sig) && !parts[1].equals(sig) ) continue;

                String message = e.getMessage();
                if( message.matches(HEMLIS_REGEX) ) message = message.replaceFirst(HEMLIS_REGEX_TRIM, "");

                String targetSig = null;
                if( message.indexOf(':') > 0 && message.indexOf(":") < 15 ){
                    targetSig = message.split(":")[0].toLowerCase();
                }

                if( parts[0].equals(targetSig) || parts[1].equals(targetSig) ){
                    retList.add(e);
                    lastTargetedSig = targetSig;
                    betweenCount = 0;
                }else if( targetSig == null &&
                        sig.equals(lastTargetedSig) ){
                    retList.add(e);
                    break;
                }else{
                    betweenCount++;
                }
            }

            Log.d("Entry filter", "Matched: "+retList.size());
            result.values = retList;
            result.count = retList.size();

            setFilteredResults(true);
            return result;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            ArrayList<Entry> filtered = (ArrayList<Entry>) results.values;
            notifyDataSetChanged();
            clear();

            for (int i = 0, l = filtered.size(); i < l; i++)
                add(filtered.get(i));

            notifyDataSetInvalidated();
        }
    };
}