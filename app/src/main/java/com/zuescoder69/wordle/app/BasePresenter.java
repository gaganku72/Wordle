package com.zuescoder69.wordle.app;

/**
 * Created by Gagan Kumar on 30/05/22.
 */
public interface BasePresenter<T extends BaseView> {
    /**
     * Binds presenter with a view when resumed. The Presenter will perform initialization here.
     *
     * @param view the view associated with this presenter
     */
    void takeView(T view);

    /**
     * Drops the reference to the view when destroyed
     */
    void dropView();
}
