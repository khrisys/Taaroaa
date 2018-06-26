// Generated code from Butter Knife. Do not modify!
package fr.drochon.christian.taaroaa.controller;

import android.support.annotation.CallSuper;
import android.support.annotation.UiThread;
import android.view.View;
import android.widget.Button;
import butterknife.Unbinder;
import butterknife.internal.Utils;
import fr.drochon.christian.taaroaa.R;
import java.lang.IllegalStateException;
import java.lang.Override;

public class SummaryActivity_ViewBinding implements Unbinder {
  private SummaryActivity target;

  @UiThread
  public SummaryActivity_ViewBinding(SummaryActivity target) {
    this(target, target.getWindow().getDecorView());
  }

  @UiThread
  public SummaryActivity_ViewBinding(SummaryActivity target, View source) {
    this.target = target;

    target.mCours = Utils.findRequiredViewAsType(source, R.id.cours_btn, "field 'mCours'", Button.class);
    target.mSortie = Utils.findRequiredViewAsType(source, R.id.sorties_btn, "field 'mSortie'", Button.class);
  }

  @Override
  @CallSuper
  public void unbind() {
    SummaryActivity target = this.target;
    if (target == null) throw new IllegalStateException("Bindings already cleared.");
    this.target = null;

    target.mCours = null;
    target.mSortie = null;
  }
}
