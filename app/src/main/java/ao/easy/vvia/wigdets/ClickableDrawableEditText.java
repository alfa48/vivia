package ao.easy.vvia.wigdets;

import android.content.Context;
import android.util.AttributeSet;

public class ClickableDrawableEditText extends androidx.appcompat.widget.AppCompatEditText {

    public ClickableDrawableEditText(Context context) {
        super(context);
    }

    public ClickableDrawableEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ClickableDrawableEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean performClick() {
        super.performClick(); // mantém comportamento padrão
        return true;
    }
}
