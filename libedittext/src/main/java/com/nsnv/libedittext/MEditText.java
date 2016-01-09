package com.nsnv.libedittext;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.support.v7.widget.AppCompatEditText;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ccsun on 2015/9/6.
 * EditText for MaterialDesign.
 *
 * Should set colorPrimary attr.
 *
 * <p/>
 * setBottomLoadingEnabled(boolean) true:false
 * if true, when this lost focus, it will show loading state
 *
 * <p/>
 * stopBottomLoading()
 * stop loading state
 *
 * <p/>
 * setBottomHint(String msg)
 * show msg below bottom line.
 *
 * <p/>
 * setError(CharSequence)
 * show red error msg.
 *
 * <p/>
 * clearBottomState()
 * clear button state.
 *
 * <p/>
 * setIntMaxCount(int)
 * Characters MAX user can input
 *
 * <p/>
 * addIUserInputWordsCheck(IUserInputWordsCheck check)
 * Add check for user input.
 * Within this function, should use setError(CharSequence) to show error message.
 */
public class MEditText extends AppCompatEditText {

    Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private int edtTextSize;
    private Context cxt;

    private float floatingLabelColorFraction = 0;
    private float floatingLabelSizeFraction = 0;
    private float bottomLoadingFraction = 0;
    private boolean isBottomLoadingEnabled = false;

    private String strHintText;
    private int hintTextColor;
    private ArgbEvaluator argbEvaluator = new ArgbEvaluator();

    private String strBottomText;
    /**
     * Characters user has input
     */
    private int intCharactersCount;

    /**
     * Characters MAX user can input
     */
    private int intMaxCount = 0;
    private List<IUserInputWordsCheck> listMaterialEdtCheck = new ArrayList<IUserInputWordsCheck>() {
    };
    private boolean isShowClearIcon;
    private int clearIconStartX;
    private int clearIconStartY;

    private enum BottomTextState {
        Nothing(0),
        HintMsg(1),
        ErrorMsg(2),
        Loading(3);
        int VALUE;
        BottomTextState(int i) {
            this.VALUE = i;
        }
    }
    private BottomTextState bottomTextState = BottomTextState.Nothing;

    private boolean isErrorCount = false;

    private int colorAccent;
    private int colorPrimary;

    public MEditText(Context context) {
        super(context);
        init(context, null);
    }

    public MEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public MEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    @Override
    protected void onDraw(Canvas canvas) {

        int startXFloatintText = getTotalPaddingLeft();

        int colorFloatingHint = colorPrimary;
        // draw floating text
        if (this.getHint() == null) {
            // set hint null when get focus

            if(this.length() == 0){
                colorFloatingHint = (int) argbEvaluator.evaluate(floatingLabelSizeFraction, hintTextColor, colorPrimary);
            }else{
                colorFloatingHint = (int) argbEvaluator.evaluate(floatingLabelColorFraction, colorPrimary, Color.GRAY&(0x8FFFFFFF));
            }
            paint.setColor(colorFloatingHint);

            float hintTextSizeFraction = (float) (floatingLabelSizeFraction * (0.618 - 1) + 1);
            float hintTextSize = edtTextSize * hintTextSizeFraction;
            paint.setTextSize(hintTextSize);

            int edtHintY = getBaseline();
            int startYFloatingText = (int) ((hintTextSize - edtHintY) * floatingLabelSizeFraction + edtHintY);
            canvas.drawText(strHintText, startXFloatintText, startYFloatingText, paint);
        }

        // draw bottom line
        int startYBottomLine = (int) (getHeight() - (getPaddingBottom()*0.8));
        if(BottomTextState.ErrorMsg== bottomTextState || isErrorCount){
            paint.setColor(colorAccent);
        }else{
            if(this.length() == 0){

                paint.setColor(colorPrimary);
            }else{
                paint.setColor(colorFloatingHint);
            }
        }
        canvas.drawRect(startXFloatintText, startYBottomLine, getWidth() + getScrollX(), startYBottomLine + dp2px(2), paint);

        // draw clear icon
        if(isShowClearIcon){

            int colorForAlpha = (int) argbEvaluator.evaluate(floatingLabelColorFraction, Color.GRAY & 0x7FFFFFFF, Color.GRAY & 0x00FFFFFF);
            paint.setColor(colorForAlpha);
            clearIconStartX = getWidth() + getScrollX() - getBitmapClearIcon().getWidth();
            clearIconStartY = startYBottomLine -  getBitmapClearIcon().getHeight() - dp2px(4);
            canvas.drawBitmap(getBitmapClearIcon(), clearIconStartX, clearIconStartY, paint);
        }

        // draw bottem text
        int startYBottomText = (int) (getHeight() - (getPaddingBottom()*0.1));
        paint.setTextSize((float) (edtTextSize * 0.618));
        if(BottomTextState.HintMsg == bottomTextState){

            paint.setColor(colorPrimary);
            canvas.drawText(strBottomText, startXFloatintText, startYBottomText, paint);

        }else if(BottomTextState.ErrorMsg == bottomTextState){

            paint.setColor(colorAccent);
            canvas.drawText(strBottomText, startXFloatintText, startYBottomText, paint);

        }else if(BottomTextState.Loading == bottomTextState && isBottomLoadingEnabled){

            int count = (int) (bottomLoadingFraction * 10 / 2);
            int radius = dp2px(3);
            for(int i = 0; i < count; i++){
                canvas.drawCircle(startXFloatintText + radius + radius * i * 4, startYBottomText - radius, radius, paint);
            }

        }else{
            // def: BottomTextState.Nothing
            strBottomText = "";
            paint.setColor(colorPrimary);
            canvas.drawText(strBottomText, startXFloatintText, startYBottomText, paint);
        }

        // draw text counter
        StringBuilder buildCounter = new StringBuilder();
        buildCounter.append(intCharactersCount);
        if(intMaxCount > 0){
            buildCounter.append(" / " + intMaxCount);
            String strCounter = buildCounter.toString();
            float hintTextSize = (float) (edtTextSize * 0.618);
            paint.setTextSize(hintTextSize);
            int startXTextCounter = (int) (getWidth()+ getScrollX() - paint.measureText(strCounter));
            if(isErrorCount){
                paint.setColor(colorAccent);
            }else {
                paint.setColor(colorFloatingHint);
            }
            canvas.drawText(strCounter, startXTextCounter, startYBottomText, paint);
        }

        super.onDraw(canvas);
    }

    private void init(Context cxt, AttributeSet attrs) {
        this.cxt = cxt;

        edtTextSize = (int) this.getTextSize();
        hintTextColor = this.getHintTextColors().getDefaultColor();

        colorPrimary = Color.DKGRAY;
        colorAccent = Color.RED;
        try {

            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){

                TypedValue typedValue = new TypedValue();
                cxt.getTheme().resolveAttribute(android.R.attr.colorPrimary, typedValue, true);
                colorPrimary = typedValue.data;
                cxt.getTheme().resolveAttribute(android.R.attr.colorAccent, typedValue, true);
                colorAccent = typedValue.data;
            }else {
                throw new RuntimeException("SDK_INT is less than Lollipop.");
            }
        }catch (Exception e){

            try {
                TypedValue typedValue = new TypedValue();
                int idColorPrimary = getResources().getIdentifier("colorPrimary", "attr", cxt.getPackageName());
                int idColorAccent = getResources().getIdentifier("colorAccent", "attr", cxt.getPackageName());
                if(0 != idColorPrimary) {
                    cxt.getTheme().resolveAttribute(idColorPrimary, typedValue, true);
                    colorPrimary = typedValue.data;
                }else {
                    throw new RuntimeException("colorPrimary not found");
                }

                if(0 != idColorAccent) {
                    cxt.getTheme().resolveAttribute(idColorAccent, typedValue, true);
                    colorAccent = typedValue.data;
                }else {
                    throw new RuntimeException("colorAccent not found");
                }
            }catch (Exception e1){
                // use default color.
            }
        }

        if(this.getHint() == null) {
            throw new NullPointerException("MaterialEditText hint cant be null");
        }else {
            strHintText = this.getHint().toString();
        }

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
            setBackground(null);
        else
            setBackgroundDrawable(null);

        this.setPadding(0, edtTextSize, 0, (int) (edtTextSize * 0.618 * 2));

        this.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {

                if(hasFocus){

                    if (((AppCompatEditText) v).length() == 0) {

                        ((AppCompatEditText) v).setHint(null);
                        getAnimatorLabelSize().start();
                    }else{

                        getAnimatorLabelColor().reverse();
                    }

                    if (isBottomLoadingEnabled) {
                        stopBottomLoading();
                    }


                }else{

                    if (((AppCompatEditText) v).length() == 0) {

                        getAnimatorLabelSize().reverse();
                    }else{

                        getAnimatorLabelColor().start();
                    }

                    boolean isAllCheckOK = true;
                    for(IUserInputWordsCheck check : listMaterialEdtCheck){
                        if(!check.isOk()){
                            isAllCheckOK = false;
                            break;
                        }
                    }

                    if (BottomTextState.ErrorMsg != bottomTextState
                            && !isErrorCount
                            && isAllCheckOK) {

                        startBottomLoading();
                    }

                }

            }
        });

        intCharactersCount = this.getText().length();
        this.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                intCharactersCount = s.length();
                if (intCharactersCount > intMaxCount && intMaxCount>0) {
                    isErrorCount = true;
                } else {
                    isErrorCount = false;
                }

                if (intCharactersCount > 0) {
                    showClearIcon(true);
                } else {
                    showClearIcon(false);
                }
            }
        });

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if(hasFocus() && isShowClearIcon){
            switch( event.getAction()){
                case MotionEvent.ACTION_UP:

                    if(!TextUtils.isEmpty(getText()) &&
                            isShowClearIcon &&
                            hasClickedClearIcon(event)){
                        setText(null);
                    }

                    break;
                default:
                    // do nothing
            }
        }

        return super.onTouchEvent(event);
    }

    private boolean hasClickedClearIcon(MotionEvent event) {

        float x = event.getX();
        float y = event.getY();

        int startX = clearIconStartX;
        int endX = clearIconStartX + getBitmapClearIcon().getWidth();

        int startY = clearIconStartY;
        int endY = clearIconStartY + getBitmapClearIcon().getHeight();

        return (x>=startX && x<=endX && y>=startY && y<=endY) ? true:false;
    }

    private void showClearIcon(boolean isShown){

        if(isShown && !isShowClearIcon){
            isShowClearIcon = true;
            this.setPadding(getPaddingLeft(), getPaddingTop(), getPaddingRight() + getBitmapClearIcon().getWidth(), getPaddingBottom());

        }else if(!isShown){
            isShowClearIcon = false;
            this.setPadding(getPaddingLeft(), getPaddingTop(), getPaddingRight() - getBitmapClearIcon().getWidth(), getPaddingBottom());
        }
    }

    // always use this object by method "get"
    private Bitmap bitmapClear;
    private Bitmap getBitmapClearIcon(){
        if(null==bitmapClear){

            BitmapFactory.Options ops = new BitmapFactory.Options();
            ops.inJustDecodeBounds = true;
            BitmapFactory.decodeResource(getResources(), R.mipmap.met_ic_clear, ops);
            int size = Math.max(ops.outWidth, ops.outHeight);
            ops.inSampleSize = size > edtTextSize ? size/edtTextSize:1;
            ops.inJustDecodeBounds = false;
            bitmapClear = BitmapFactory.decodeResource(getResources(), R.mipmap.met_ic_clear, ops);

        }

        return bitmapClear;
    }

    private ObjectAnimator animatorLabelColor;
    private ObjectAnimator getAnimatorLabelColor(){
        if(null == animatorLabelColor) {
            animatorLabelColor = ObjectAnimator.ofFloat(this, "floatingLabelColorFraction", 0, 1f);
        }
        animatorLabelColor.setDuration(800);
        return animatorLabelColor;
    }

    // always use this object by method "get"
    private ObjectAnimator animotorLabelSize;
    private ObjectAnimator getAnimatorLabelSize() {
        if(null == animotorLabelSize){
            animotorLabelSize = ObjectAnimator.ofFloat(this, "floatingLabelSizeFraction", 0, 1f);
        }
        animotorLabelSize.setDuration(800);
        return animotorLabelSize;
    }

    // always use this object by method "get"
    private ObjectAnimator animotorBottomLoading;
    private ObjectAnimator getBottomLoadingAnimator(){
        if(null == animotorBottomLoading){
            animotorBottomLoading = ObjectAnimator.ofFloat(this, "bottomLoadingFraction", 0, 1f);
        }
        animotorBottomLoading.setDuration(3000);
        animotorBottomLoading.setRepeatMode(Animation.RESTART);
        animotorBottomLoading.setRepeatCount(Animation.INFINITE);
        return animotorBottomLoading;
    }

    private int dp2px(int dp) {
        Resources r = cxt.getResources();
        float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics());
        return Math.round(px);
    }

    @Override
    public void setError(CharSequence error) {
//        super.setError(error);
        strBottomText = error.toString();
        bottomTextState = BottomTextState.ErrorMsg;
        invalidate();
    }

    public void setBottomHint(String bottomHint) {
        this.strBottomText = bottomHint;
        bottomTextState = BottomTextState.HintMsg;
        invalidate();
    }

    /**
     * @param intMaxCount
     */
    public void setIntMaxCount(int intMaxCount) {
        this.intMaxCount = intMaxCount;
    }

    /**
     * @param enabled
     *  true: enable bottom loading state.
     *  false: disable bottom loading state.
     */
    public void setBottomLoadingEnabled(boolean enabled){
        isBottomLoadingEnabled = enabled;
    }

    /**
     * Start animator for loading msg
     */
    private void startBottomLoading() {
        bottomTextState = BottomTextState.Loading;
        getBottomLoadingAnimator().start();
    }

    /**
     * Stop animator for loading msg
     */
    public void stopBottomLoading(){

        getBottomLoadingAnimator().cancel();

        bottomTextState = BottomTextState.Nothing;
        invalidate();
    }

    /**
     * Clear Bottom Hint Msg
     */
    public void clearBottomState(){
        bottomTextState = BottomTextState.Nothing;
    }

    /**
     * Setter for Animation.
     */
    public void setFloatingLabelColorFraction(float floatingLabelColorFraction) {
        this.floatingLabelColorFraction = floatingLabelColorFraction;
        invalidate();
    }

    /**
     * Setter for Animation.
     */
    public void setFloatingLabelSizeFraction(float floatingLabelSizeFraction) {
        this.floatingLabelSizeFraction = floatingLabelSizeFraction;
        invalidate();
    }

    /**
     * Setter for Animation.
     */
    public void setBottomLoadingFraction(float bottomLoadingFraction) {
        this.bottomLoadingFraction = bottomLoadingFraction;
        invalidate();
    }

    public void addIUserInputWordsCheck(IUserInputWordsCheck check){
        if(null==check)
            throw new NullPointerException("IUserInputWordsCheck cant be null");
        listMaterialEdtCheck.add(check);
    }

    public interface IUserInputWordsCheck{
        abstract boolean isOk();
    }
}
