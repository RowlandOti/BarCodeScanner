package it.jaschke.alexandria.ui.fragments;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import butterknife.Bind;
import butterknife.ButterKnife;
import it.jaschke.alexandria.R;
import it.jaschke.alexandria.provider.AlexandriaContract;
import it.jaschke.alexandria.services.BookService;
import it.jaschke.alexandria.services.DownloadImage;
import it.jaschke.alexandria.ui.activities.ScanActivity;
import it.jaschke.alexandria.utilities.NetworkUtility;


public class AddBookFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final String REQ_SCAN_RESULTS = "scanResults";
    public static final int REQ_SCAN_CODE = 1;

    private final int LOADER_ID = 1;
    private final String EAN_CONTENT = "eanContent";
    // ButterKnife injected views
    // The surface view containing layout
    @Bind(R.id.scan_button)
    Button mBookScanButton;
    @Bind(R.id.save_button)
    Button mBookSaveButton;
    @Bind(R.id.delete_button)
    Button mBookDeleteButton;
    @Bind(R.id.ean)
    EditText mBookNoEditText;
    @Bind(R.id.bookTitle)
    TextView mBookTitleTextView;
    @Bind(R.id.bookSubTitle)
    TextView mBookSubTitleTextView;
    @Bind(R.id.bookCover)
    ImageView mBookCoverImageView;
    @Bind(R.id.bookAuthors)
    TextView mBookAuthorsTextView;
    @Bind(R.id.bookCategories)
    TextView mBookCategoriesTextView;

    public AddBookFragment() {
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mBookNoEditText != null) {
            outState.putString(EAN_CONTENT, mBookNoEditText.getText().toString());
        }
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_add_book, container, false);
        // Inflate all views
        ButterKnife.bind(this, rootView);

        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mBookNoEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                //no need
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //no need
            }

            @Override
            public void afterTextChanged(Editable s) {
                String ean = s.toString();
                //catch isbn10 numbers
                if (ean.length() == 10 && !ean.startsWith("978")) {
                    ean = "978" + ean;
                }
                if (ean.length() < 13) {
                    clearFields();
                    return;
                }
                // Check network connectivity
                if (!NetworkUtility.isNetworkAvailable(getActivity())) {
                    // Alert user , we need internet
                    Toast.makeText(getActivity(), "Internet Connectivity is Unavailable", Toast.LENGTH_SHORT).show();
                    return;
                }
                //Once we have an ISBN, start a book intent
                Intent bookIntent = new Intent(getActivity(), BookService.class);
                bookIntent.putExtra(BookService.EAN, ean);
                bookIntent.setAction(BookService.FETCH_BOOK);
                getActivity().startService(bookIntent);
                AddBookFragment.this.restartLoader();
            }
        });

        mBookScanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create an Intent object
                //Intent intent = new Intent(getActivity(), ScanActivity.class);
                // Start the DetailActivity
                //startActivity(intent);
                Intent scanIntent = new Intent(getActivity(), ScanActivity.class);
                startActivityForResult(scanIntent, REQ_SCAN_CODE);
            }
        });

        mBookSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mBookNoEditText.setText("");
            }
        });

        mBookDeleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent bookIntent = new Intent(getActivity(), BookService.class);
                bookIntent.putExtra(BookService.EAN, mBookNoEditText.getText().toString());
                bookIntent.setAction(BookService.DELETE_BOOK);
                getActivity().startService(bookIntent);
                mBookNoEditText.setText("");
            }
        });

        if (savedInstanceState != null) {
            mBookNoEditText.setText(savedInstanceState.getString(EAN_CONTENT));
            mBookNoEditText.setHint("");
        }
    }

    private void restartLoader() {
        getLoaderManager().restartLoader(LOADER_ID, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (mBookNoEditText.getText().length() == 0) {
            return null;
        }
        String eanStr = mBookNoEditText.getText().toString();
        if (eanStr.length() == 10 && !eanStr.startsWith("978")) {
            eanStr = "978" + eanStr;
        }
        return new CursorLoader(
                getActivity(),
                AlexandriaContract.BookEntry.buildFullBookUri(Long.parseLong(eanStr)),
                null,
                null,
                null,
                null
        );
    }

    @Override
    public void onLoadFinished(android.support.v4.content.Loader<Cursor> loader, Cursor data) {
        if (!data.moveToFirst()) {
            return;
        }

        String bookTitleText = data.getString(data.getColumnIndex(AlexandriaContract.BookEntry.TITLE));
        mBookTitleTextView.setText(bookTitleText);

        String bookSubTitleText = data.getString(data.getColumnIndex(AlexandriaContract.BookEntry.SUBTITLE));
        mBookSubTitleTextView.setText(bookSubTitleText);

        String authors = data.getString(data.getColumnIndex(AlexandriaContract.AuthorEntry.AUTHOR));
        if (authors != null) {
            String[] authorsArr = authors.split(",");
            mBookAuthorsTextView.setLines(authorsArr.length);
            mBookAuthorsTextView.setText(authors.replace(",", "\n"));
        }
        String imgUrl = data.getString(data.getColumnIndex(AlexandriaContract.BookEntry.IMAGE_URL));
        if (Patterns.WEB_URL.matcher(imgUrl).matches()) {
            new DownloadImage(mBookCoverImageView).execute(imgUrl);
            mBookCoverImageView.setVisibility(View.VISIBLE);
        }

        String categoriesText = data.getString(data.getColumnIndex(AlexandriaContract.CategoryEntry.CATEGORY));
        mBookCategoriesTextView.setText(categoriesText);

        mBookSaveButton.setVisibility(View.VISIBLE);
        mBookDeleteButton.setVisibility(View.VISIBLE);
    }

    @Override
    public void onLoaderReset(android.support.v4.content.Loader<Cursor> loader) {

    }

    private void clearFields() {
        mBookTitleTextView.setText("");
        mBookSubTitleTextView.setText("");
        mBookAuthorsTextView.setText("");
        mBookCategoriesTextView.setText("");
        mBookCoverImageView.setVisibility(View.INVISIBLE);
        mBookSaveButton.setVisibility(View.INVISIBLE);
        mBookDeleteButton.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        activity.setTitle(R.string.scan);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent dataIntent) {

        super.onActivityResult(requestCode, resultCode, dataIntent);

        // The sign up activity returned that the user has successfully created an account
        if (requestCode == AddBookFragment.REQ_SCAN_CODE && resultCode == getActivity().RESULT_OK) {
            String scanResult = dataIntent.getStringExtra(REQ_SCAN_RESULTS);
            mBookNoEditText.setText(scanResult);
        }
    }
}
