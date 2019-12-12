package com.fuxi.util.loadImages;

import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import android.graphics.Bitmap;

public class MemoryCache {
    // 最大的缓存数
    private static final int MAX_CACHE_CAPACITY = 30;
    // 用Map软引用的Bitmap对象, 保证内存空间足够情况下不会被垃圾回收
    private HashMap<String, SoftReference<Bitmap>> mCacheMap = new LinkedHashMap<String, SoftReference<Bitmap>>() {
        private static final long serialVersionUID = 1L;

        // 当缓存数量超过规定大小（返回true）会清除最早放入缓存的
        protected boolean removeEldestEntry(Map.Entry<String, SoftReference<Bitmap>> eldest) {
            return size() > MAX_CACHE_CAPACITY;
        };
    };

    /**
     * 从缓存里取出图片
     * 
     * @param id
     * @return 如果缓存有，并且该图片没被释放，则返回该图片，否则返回null
     */
    public Bitmap get(String id) {
        if (!mCacheMap.containsKey(id))
            return null;
        SoftReference<Bitmap> ref = mCacheMap.get(id);
        return ref.get();
    }

    /**
     * 将图片加入缓存
     * 
     * @param id
     * @param bitmap
     */
    public void put(String id, Bitmap bitmap) {
        mCacheMap.put(id, new SoftReference<Bitmap>(bitmap));
    }

    /**
     * 清除所有缓存
     */
    public void clear() {
        try {
            for (Map.Entry<String, SoftReference<Bitmap>> entry : mCacheMap.entrySet()) {
                SoftReference<Bitmap> sr = entry.getValue();
                if (null != sr) {
                    Bitmap bmp = sr.get();
                    if (null != bmp)
                        bmp.recycle();
                }
            }
            mCacheMap.clear();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
