package com.example.siddharth.popmovieapp;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.graphics.Palette;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class MovieDescriptionFragment extends Fragment {

    public MovieDescriptionFragment() {
    }

    private Date ConvertToDate(String dateString){
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date convertedDate = new Date();
        try {
            convertedDate = dateFormat.parse(dateString);
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return convertedDate;
    }

   @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_movie_description, container, false);
        Bundle b = getActivity().getIntent().getExtras();

        Movie movie = b.getParcelable("MOVIE");


        if (movie == null) {
            //NO MOVIE
        } else {


            String checker = "empty";

            int backdropWidth = Util.getScreenWidth(getActivity());
            int backdropHeight = getResources().getDimensionPixelSize(R.dimen.details_backdrop_height);


            ImageView view_Backdrop = (ImageView) rootView.findViewById(R.id.mBackdrop);
            if (movie.getBackdrop_path().equals(checker)) {
                Picasso.with(getActivity()).load(R.drawable.image_not_found).into(view_Backdrop);
            }
            else
            {
                Picasso.with(getActivity()).load(Util.buildBackdropUrl(movie.getBackdrop_path(), backdropWidth))
                        .resize(backdropWidth, backdropHeight)
                        .centerCrop()
                        .transform(PaletteTransformation.instance())
                        .into(view_Backdrop, new ActivityPaletteTransformation(view_Backdrop));
            }


            int posterWidth = getResources().getDimensionPixelSize(R.dimen.details_poster_width);
            int posterHeight = getResources().getDimensionPixelSize(R.dimen.details_poster_height);

            ImageView view_Poster = (ImageView) rootView.findViewById(R.id.mPoster);
            if (movie.getPoster_path().equals(checker)) {
                Picasso.with(getActivity()).load(R.drawable.image_not_found).into(view_Poster);
            }
            else
            {
                Picasso.with(getActivity()).load(movie.getPoster_path()).resize(posterWidth, posterHeight).centerCrop().into(view_Poster);
            }






            ((TextView) rootView.findViewById(R.id.mSynopsis))
                    .setText(movie.getDescription());

            ((TextView) rootView.findViewById(R.id.mTitle))
                    .setText(movie.getTitle());

            ((TextView) rootView.findViewById(R.id.mRating))
                    .setText(movie.getVote_average());

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(ConvertToDate(movie.getRelease_date()));

            ((TextView) rootView.findViewById(R.id.mRelease))
                    .setText(String.valueOf(calendar.get(Calendar.YEAR)));;
        }

        return rootView;
    }
    class ActivityPaletteTransformation extends PaletteTransformation.Callback {
        public ActivityPaletteTransformation(@NonNull ImageView imageView) {
            super(imageView);
        }

        @Override
        protected void onSuccess(Palette palette) {
            Activity activity = getActivity();
            if (activity instanceof PaletteCallback) {
                PaletteCallback callback = (PaletteCallback) activity;
                callback.setPrimaryColor(palette);
            }
        }

        @Override
        public void onError() {}
    }

    public interface PaletteCallback {
        void setPrimaryColor(Palette palette);
    }

}
