package com.alertdialogpro.material;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.LruCache;
import android.util.Log;
import android.util.TypedValue;
import com.alertdialogpro.R;

public class TintManager
  {
  private static final String              TAG                            =TintManager.class.getSimpleName();
  private static final boolean             DEBUG                          =false;
  static final PorterDuff.Mode             DEFAULT_MODE                   =PorterDuff.Mode.SRC_IN;
  private static final ColorFilterLruCache COLOR_FILTER_CACHE             =new ColorFilterLruCache(6);
  /**
   * Drawables which should be tinted with the value of {@code R.attr.colorControlNormal},
   * using the default mode.
   */
  private static final int[]               TINT_COLOR_CONTROL_NORMAL      = {R.drawable.abc_ic_ab_back_mtrl_am_alpha,R.drawable.abc_ic_go_search_api_mtrl_alpha,R.drawable.abc_ic_search_api_mtrl_alpha,R.drawable.abc_ic_commit_search_api_mtrl_alpha,R.drawable.abc_ic_clear_mtrl_alpha,R.drawable.abc_ic_menu_share_mtrl_alpha,R.drawable.abc_ic_menu_copy_mtrl_am_alpha,R.drawable.abc_ic_menu_cut_mtrl_alpha,R.drawable.abc_ic_menu_selectall_mtrl_alpha,R.drawable.abc_ic_menu_paste_mtrl_am_alpha,R.drawable.abc_ic_menu_moreoverflow_mtrl_alpha,R.drawable.abc_ic_voice_search_api_mtrl_alpha,R.drawable.abc_textfield_search_default_mtrl_alpha,R.drawable.abc_textfield_default_mtrl_alpha};
  /**
   * Drawables which should be tinted with the value of {@code R.attr.colorControlActivated},
   * using the default mode.
   */
  private static final int[]               TINT_COLOR_CONTROL_ACTIVATED   = {R.drawable.abc_textfield_activated_mtrl_alpha,R.drawable.abc_textfield_search_activated_mtrl_alpha,R.drawable.abc_cab_background_top_mtrl_alpha};
  /**
   * Drawables which should be tinted with the value of {@code android.R.attr.colorBackground},
   * using the {@link android.graphics.PorterDuff.Mode#MULTIPLY} mode.
   */
  private static final int[]               TINT_COLOR_BACKGROUND_MULTIPLY = {R.drawable.abc_popup_background_mtrl_mult,R.drawable.abc_cab_background_internal_bg,R.drawable.abc_menu_hardkey_panel_mtrl_mult};
  /**
   * Drawables which should be tinted using a state list containing values of {@code R.attr.colorControlNormal} and {@code R.attr.colorControlActivated}
   */
  private static final int[]               TINT_COLOR_CONTROL_STATE_LIST  = {R.drawable.abc_edit_text_material,R.drawable.abc_tab_indicator_material,R.drawable.abc_textfield_search_material,R.drawable.abc_spinner_mtrl_am_alpha,R.drawable.abc_btn_check_material,R.drawable.abc_btn_radio_material};
  /**
   * Drawables which contain other drawables which should be tinted. The child drawable IDs
   * should be defined in one of the arrays above.
   */
  private static final int[]               CONTAINERS_WITH_TINT_CHILDREN  = {R.drawable.abc_cab_background_top_material};
  private final Context                    mContext;
  private final Resources                  mResources;
  private final TypedValue                 mTypedValue;
  private ColorStateList                   mDefaultColorStateList;
  private ColorStateList                   mSwitchThumbStateList;
  private ColorStateList                   mSwitchTrackStateList;

  /**
   * A helper method to instantiate a {@link TintManager} and then call {@link #getDrawable(int)}.
   * This method should not be used routinely.
   */
  public static Drawable getDrawable(final Context context,final int resId)
    {
    if(isInTintList(resId))
      return new TintManager(context).getDrawable(resId);
    else return ContextCompat.getDrawable(context,resId);
    }

  public TintManager(final Context context)
    {
    mContext=context;
    mResources=new TintResources(context.getResources(),this);
    mTypedValue=new TypedValue();
    }

  public Drawable getDrawable(final int resId)
    {
    Drawable drawable=ContextCompat.getDrawable(mContext,resId);
    if(drawable!=null)
      if(arrayContains(TINT_COLOR_CONTROL_STATE_LIST,resId))
        drawable=new TintDrawableWrapper(drawable,getDefaultColorStateList());
      else if(resId==R.drawable.abc_switch_track_mtrl_alpha)
        drawable=new TintDrawableWrapper(drawable,getSwitchTrackColorStateList());
      else if(resId==R.drawable.abc_switch_thumb_material)
        drawable=new TintDrawableWrapper(drawable,getSwitchThumbColorStateList(),PorterDuff.Mode.MULTIPLY);
      else if(arrayContains(CONTAINERS_WITH_TINT_CHILDREN,resId))
        drawable=mResources.getDrawable(resId);
      else tintDrawable(resId,drawable);
    return drawable;
    }

  void tintDrawable(final int resId,final Drawable drawable)
    {
    PorterDuff.Mode tintMode=null;
    boolean colorAttrSet=false;
    int colorAttr=0;
    int alpha=-1;
    if(arrayContains(TINT_COLOR_CONTROL_NORMAL,resId))
      {
      colorAttr=R.attr.colorControlNormal;
      colorAttrSet=true;
      }
    else if(arrayContains(TINT_COLOR_CONTROL_ACTIVATED,resId))
      {
      colorAttr=R.attr.colorControlActivated;
      colorAttrSet=true;
      }
    else if(arrayContains(TINT_COLOR_BACKGROUND_MULTIPLY,resId))
      {
      colorAttr=android.R.attr.colorBackground;
      colorAttrSet=true;
      tintMode=PorterDuff.Mode.MULTIPLY;
      }
    else if(resId==R.drawable.abc_list_divider_mtrl_alpha)
      {
      colorAttr=android.R.attr.colorForeground;
      colorAttrSet=true;
      alpha=Math.round(0.16f*255);
      }
    if(colorAttrSet)
      {
      if(tintMode==null)
        tintMode=DEFAULT_MODE;
      final int color=getThemeAttrColor(colorAttr);
      // First, lets see if the cache already contains the color filter
      PorterDuffColorFilter filter=COLOR_FILTER_CACHE.get(color,tintMode);
      if(filter==null)
        {
        // Cache miss, so create a color filter and add it to the cache
        filter=new PorterDuffColorFilter(color,tintMode);
        COLOR_FILTER_CACHE.put(color,tintMode,filter);
        }
      // Finally set the color filter
      drawable.setColorFilter(filter);
      if(alpha!=-1)
        drawable.setAlpha(alpha);
      if(DEBUG)
        Log.d(TAG,"Tinted Drawable ID: "+mResources.getResourceName(resId)+" with color: #"+Integer.toHexString(color));
      }
    }

  private static boolean arrayContains(final int[] array,final int value)
    {
    for(final int id : array)
      if(id==value)
        return true;
    return false;
    }

  private static boolean isInTintList(final int drawableId)
    {
    return arrayContains(TINT_COLOR_BACKGROUND_MULTIPLY,drawableId)||arrayContains(TINT_COLOR_CONTROL_NORMAL,drawableId)||arrayContains(TINT_COLOR_CONTROL_ACTIVATED,drawableId)||arrayContains(TINT_COLOR_CONTROL_STATE_LIST,drawableId)||arrayContains(CONTAINERS_WITH_TINT_CHILDREN,drawableId);
    }

  private ColorStateList getDefaultColorStateList()
    {
    if(mDefaultColorStateList==null)
      {
      /**
       * Generate the default color state list which uses the colorControl attributes.
       * Order is important here. The default enabled state needs to go at the bottom.
       */
      final int colorControlNormal=getThemeAttrColor(R.attr.colorControlNormal);
      final int colorControlActivated=getThemeAttrColor(R.attr.colorControlActivated);
      final int[][] states=new int[7][];
      final int[] colors=new int[7];
      int i=0;
      // Disabled state
      states[i]=new int[] {-android.R.attr.state_enabled};
      colors[i]=getDisabledThemeAttrColor(R.attr.colorControlNormal);
      i++;
      states[i]=new int[] {android.R.attr.state_focused};
      colors[i]=colorControlActivated;
      i++;
      states[i]=new int[] {android.R.attr.state_activated};
      colors[i]=colorControlActivated;
      i++;
      states[i]=new int[] {android.R.attr.state_pressed};
      colors[i]=colorControlActivated;
      i++;
      states[i]=new int[] {android.R.attr.state_checked};
      colors[i]=colorControlActivated;
      i++;
      states[i]=new int[] {android.R.attr.state_selected};
      colors[i]=colorControlActivated;
      i++;
      // Default enabled state
      states[i]=new int[0];
      colors[i]=colorControlNormal;
      i++;
      mDefaultColorStateList=new ColorStateList(states,colors);
      }
    return mDefaultColorStateList;
    }

  private ColorStateList getSwitchTrackColorStateList()
    {
    if(mSwitchTrackStateList==null)
      {
      final int[][] states=new int[3][];
      final int[] colors=new int[3];
      int i=0;
      // Disabled state
      states[i]=new int[] {-android.R.attr.state_enabled};
      colors[i]=getThemeAttrColor(android.R.attr.colorForeground,0.1f);
      i++;
      states[i]=new int[] {android.R.attr.state_checked};
      colors[i]=getThemeAttrColor(R.attr.colorControlActivated,0.3f);
      i++;
      // Default enabled state
      states[i]=new int[0];
      colors[i]=getThemeAttrColor(android.R.attr.colorForeground,0.3f);
      i++;
      mSwitchTrackStateList=new ColorStateList(states,colors);
      }
    return mSwitchTrackStateList;
    }

  private ColorStateList getSwitchThumbColorStateList()
    {
    if(mSwitchThumbStateList==null)
      {
      final int[][] states=new int[3][];
      final int[] colors=new int[3];
      int i=0;
      // Disabled state
      states[i]=new int[] {-android.R.attr.state_enabled};
      colors[i]=getDisabledThemeAttrColor(R.attr.colorSwitchThumbNormal);
      i++;
      states[i]=new int[] {android.R.attr.state_checked};
      colors[i]=getThemeAttrColor(R.attr.colorControlActivated);
      i++;
      // Default enabled state
      states[i]=new int[0];
      colors[i]=getThemeAttrColor(R.attr.colorSwitchThumbNormal);
      i++;
      mSwitchThumbStateList=new ColorStateList(states,colors);
      }
    return mSwitchThumbStateList;
    }

  int getThemeAttrColor(final int attr)
    {
    if(mContext.getTheme().resolveAttribute(attr,mTypedValue,true))
      if(mTypedValue.type>=TypedValue.TYPE_FIRST_INT&&mTypedValue.type<=TypedValue.TYPE_LAST_INT)
        return mTypedValue.data;
      else if(mTypedValue.type==TypedValue.TYPE_STRING)
        return mResources.getColor(mTypedValue.resourceId);
    return 0;
    }

  int getThemeAttrColor(final int attr,final float alpha)
    {
    final int color=getThemeAttrColor(attr);
    final int originalAlpha=Color.alpha(color);
    // Return the color, multiplying the original alpha by the disabled value
    return color&0x00ffffff|Math.round(originalAlpha*alpha)<<24;
    }

  int getDisabledThemeAttrColor(final int attr)
    {
    // Now retrieve the disabledAlpha value from the theme
    mContext.getTheme().resolveAttribute(android.R.attr.disabledAlpha,mTypedValue,true);
    final float disabledAlpha=mTypedValue.getFloat();
    return getThemeAttrColor(attr,disabledAlpha);
    }

  private static class ColorFilterLruCache extends LruCache<Integer,PorterDuffColorFilter>
    {
    public ColorFilterLruCache(final int maxSize)
      {
      super(maxSize);
      }

    PorterDuffColorFilter get(final int color,final PorterDuff.Mode mode)
      {
      return get(generateCacheKey(color,mode));
      }

    PorterDuffColorFilter put(final int color,final PorterDuff.Mode mode,final PorterDuffColorFilter filter)
      {
      return put(generateCacheKey(color,mode),filter);
      }

    private static int generateCacheKey(final int color,final PorterDuff.Mode mode)
      {
      int hashCode=1;
      hashCode=31*hashCode+color;
      hashCode=31*hashCode+mode.hashCode();
      return hashCode;
      }
    }
  }
