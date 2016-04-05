package iutsd.android.tp2.saunier_debes_brice.library;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment permettant l'affichage d'une liste de livre
 * avec 3 boutons par item (details, actions, modifier)
 * IMPORTANT : L'activité qui intègre ce fragment doit
 * implémenter les interfaces suivantes :
 * - BooksListFragment.OnListFragmentInteractionListener : pour que l'activité
 * soit notifiée lors du click sur un bouton (principe de
 * délégation des actions vu en cours)
 * - BooksListFragment.BooksListProvider : pour que ce fragment puisse récupérer
 * la liste de livres depuis l'activité
 */
public class BooksListFragment extends Fragment {

  /*le RecyclerView*/
  private RecyclerView mRecyclerView;

  /*la liste des livres*/
  private List<Book>     mBooksList = new ArrayList<Book>();
  
  /*l'action de tri (sur l'id)*/
  private Book.SORT_ENUM sort       = Book.SORT_ENUM.ID;

  private int mColumnCount = 1;

  private OnListFragmentInteractionListener mListener;
  private BooksListProvider                 mBooksListProvider;

  /**
   * Constructeur obligatoire pour l'instantiation par le fragment manager
   */
  public BooksListFragment() {
  }


  /**
   * Création de la vue et donc de la liste (utilise un RecyclerView)
   *
   * @param inflater
   * @param container
   * @param savedInstanceState
   *
   * @return la vue
   */
  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.fragment_books_list, container, false);

    // mise en place de l'adapter du RecyclerView : MyBookItemRecyclerViewAdapter (similaire à un BaseAdapter)
    if (view instanceof RecyclerView) {
      Context context = view.getContext();
      mRecyclerView = (RecyclerView) view;
      if (mColumnCount <= 1) {
        mRecyclerView.setLayoutManager(new LinearLayoutManager(context));
      } else {
        mRecyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
      }
      mRecyclerView.setAdapter(new MyBookItemRecyclerViewAdapter(mBooksList, mListener));
    }

    //récupération de l'état du tri (sauvegardé dans le onSaveInstanceState)
    if (savedInstanceState != null) {
      this.sort = (Book.SORT_ENUM) savedInstanceState.getSerializable("Sort");

    }

    //effectue la mise à jour de la liste (actualisation des livres si changements)
    update();

    return view;
  }

  /**
   * Sauvegarde l'état du fragment (pour le changement d'orientation de la tablette, par exemple,
   * puisque l'activité parente est recréée, le fragment l'est aussi)
   *
   * @param savedInstanceState
   */
  @Override
  public void onSaveInstanceState(Bundle savedInstanceState) {
    // Always call the superclass so it can save the view hierarchy state
    super.onSaveInstanceState(savedInstanceState);

    // Save the user's current game state
    savedInstanceState.putSerializable("Sort", sort);
  }


  /**
   * Le callback onAttach est appelé, comme son nom l'indique, au moment où le fragment est
   * attaché à l'activité. C'est donc dans cette méthode que nous allons définir notre activité
   * parente comme étant "listener" des actions (OnListFragmentInteractionListener) ainsi que
   * fournisseur de livres (BooksListProvider)
   *
   * @param context
   */
  @Override
  public void onAttach(Context context) {
    super.onAttach(context);
    if (context instanceof OnListFragmentInteractionListener) {
      mListener = (OnListFragmentInteractionListener) context;
    } else {
      throw new RuntimeException(
          context.toString() + " must implement OnListFragmentInteractionListener");
    }

    if (context instanceof BooksListProvider) {
      mBooksListProvider = ((BooksListProvider) context);

    } else {
      throw new RuntimeException(
          context.toString() + " must implement BooksListProvider interface");
    }

  }

  @Override
  public void onDetach() {
    super.onDetach();
    mListener = null;
    mBooksListProvider = null;
  }

  /**
   * Methode permettant le tri des livre soit par nom, soit par id (cf. Book.SORT_ENUM)
   *
   * @param sort
   */
  public void sortList(Book.SORT_ENUM sort) {
    this.sort = sort;

    update();
  }


  /**
   * méthode permettant d'effectuer la mise à jour de la liste de livres
   */
  public void update() {
    mBooksList.clear();
    mBooksList.addAll(mBooksListProvider.getBooksList());
    Book.SORT(mBooksList, sort);


    mRecyclerView.getAdapter().notifyDataSetChanged();
    mRecyclerView.invalidate();
  }








  /**
   * Interface définissant la listes de actions pour lesquelles l'activité parentes sera notifiée
   * en tant que OnListFragmentInteractionListener
   */
  public interface OnListFragmentInteractionListener {
    void onClickBookDetails(Book book);

    void onClickBookModify(Book book);

    void onClickBookActions(Book book);
  }


  /**
   * Interface définissant les méthodes (la méthode ici) que l'activité parente devra implémenté
   * en tant que BooksListProvider
   */
  public interface BooksListProvider {
    List<Book> getBooksList();
  }


  /**
   * Ce RecyclerViewAdapter, à l'instar d'un Base Adapter ou d'un ArrayAdapter, fait la jonction
   * entre les données (ici les livres) et leur affichage dans la liste (RecyclerView)
   */
  private class MyBookItemRecyclerViewAdapter
      extends RecyclerView.Adapter<MyBookItemRecyclerViewAdapter.ViewHolder> {

    private final List<Book>                                          mValues;
    private final BooksListFragment.OnListFragmentInteractionListener mListener;

    /**
     * Constructeur de l'adapter
     *
     * @param items    : la liste de livres
     * @param listener : le listener des actions
     */
    public MyBookItemRecyclerViewAdapter(List<Book> items,
        BooksListFragment.OnListFragmentInteractionListener listener) {
      mValues = items;
      mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
      View view = LayoutInflater.from(parent.getContext())
          .inflate(R.layout.fragment_bookitem, parent, false);
      return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
      holder.mItem = mValues.get(position);
      holder.mBookTitle.setText(mValues.get(position).getBookName());


      holder.mDetailsButton.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
          if (null != mListener)
            mListener.onClickBookDetails(holder.mItem);
        }
      });

      holder.mActionsButton.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
          if (null != mListener)
            mListener.onClickBookActions(holder.mItem);
        }
      });

      holder.mModifyButton.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
          if (null != mListener)
            mListener.onClickBookModify(holder.mItem);
        }
      });
    }

    @Override
    public int getItemCount() {
      return mValues.size();
    }

    public class ViewHolder
        extends RecyclerView.ViewHolder {
      public final View        mView;
      public final TextView    mBookTitle;
      public final ImageButton mDetailsButton;
      public final ImageButton mModifyButton;
      public final ImageButton mActionsButton;
      public       Book        mItem;

      public ViewHolder(View view) {
        super(view);
        mView = view;
        mBookTitle = (TextView) view.findViewById(R.id.titleTextView);
        mDetailsButton = (ImageButton) view.findViewById(R.id.detailsButton);
        mModifyButton = (ImageButton) view.findViewById(R.id.saveButton);
        mActionsButton = (ImageButton) view.findViewById(R.id.actionButton);
      }

      @Override
      public String toString() {
        return super.toString() + " '" + mBookTitle.getText() + "'";
      }
    }
  }

}
