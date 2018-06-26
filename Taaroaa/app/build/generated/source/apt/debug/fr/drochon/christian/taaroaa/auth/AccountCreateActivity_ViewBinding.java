// Generated code from Butter Knife. Do not modify!
package fr.drochon.christian.taaroaa.auth;

import android.support.annotation.CallSuper;
import android.support.annotation.UiThread;
import android.view.View;
import android.widget.Button;
import butterknife.Unbinder;
import butterknife.internal.Utils;
import fr.drochon.christian.taaroaa.R;
import java.lang.IllegalStateException;
import java.lang.Override;

public class AccountCreateActivity_ViewBinding implements Unbinder {
  private AccountCreateActivity target;

  @UiThread
  public AccountCreateActivity_ViewBinding(AccountCreateActivity target) {
    this(target, target.getWindow().getDecorView());
  }

  @UiThread
  public AccountCreateActivity_ViewBinding(AccountCreateActivity target, View source) {
    this.target = target;

    target.mSuppressionCompte = Utils.findRequiredViewAsType(source, R.id.suppression_compte_btn, "field 'mSuppressionCompte'", Button.class);
  }

  @Override
  @CallSuper
  public void unbind() {
    AccountCreateActivity target = this.target;
    if (target == null) throw new IllegalStateException("Bindings already cleared.");
    this.target = null;

    target.mSuppressionCompte = null;
  }
}
