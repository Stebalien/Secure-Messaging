/*
 *  Borrowed from: https://github.com/sosiouxme/Android-examples/blob/master/ClickCounter/src/com/kagii/clickcounter/data/SimpleQueryAdapter.java
 *  The parent project is from: http://ormlite.com/android/examples/ (the ClickCounter)
 *  This page indicates that all linked content is under the CC-BY-SA 3.0 license.
 *  
 *  Technically, the MIT/X11 license (this projects) is not an approved compatible license
 *  but thats mostly because CC-BY-SA isn't even meant for software and there are no approved compatible licenses.
 *  
 *  Attribute: Gray Watson (http://256.com/gray/) for the forked project.
 *  Attribute: sosiouxme for this file.
 *  
 *  (modified quite a bit by me)
 */

package edu.mit.securemessaging.widgets;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.widget.ResourceCursorAdapter;

import com.j256.ormlite.android.AndroidCompiledStatement;
import com.j256.ormlite.android.AndroidDatabaseResults;
import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.ObjectCache;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.StatementBuilder;

public abstract class SimpleQueryAdapter<T> extends ResourceCursorAdapter {

    private final PreparedQuery<T> mQuery;
    private final ObjectCache cache;
    private final Context context;
    private final Constructor<? extends Mapper> MapConstructor;

    public SimpleQueryAdapter(Activity caller, int layout, Class<T> clazz, PreparedQuery<T> q, OrmLiteSqliteOpenHelper dbh, Class<? extends Mapper> mapper) throws SQLException {
        super(caller, layout, ((AndroidCompiledStatement) q.compile(dbh.getConnectionSource().getReadOnlyConnection(), StatementBuilder.StatementType.SELECT)).getCursor());
        mQuery = q;
        caller.startManagingCursor(getCursor());
        cache = dbh.getDao(clazz).getObjectCache();
        context = caller;
        
        try {
            MapConstructor = mapper.getDeclaredConstructor(this.getClass());
            MapConstructor.setAccessible(true);
        } catch (SecurityException e) {
            throw new RuntimeException(e);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void bindView(View listItem, Context context, Cursor cursor) {
        try {
            T dto = mQuery.mapRow(new AndroidDatabaseResults(cursor, cache));
            @SuppressWarnings("unchecked")
            Mapper map = (Mapper) listItem.getTag();
            if (map == null) {
                try {
                    map = MapConstructor.newInstance(this);
                    map.setView(listItem);
                    //This should never fail.
                } catch (IllegalArgumentException e) {
                    throw new RuntimeException(e);
                } catch (SecurityException e) {
                    throw new RuntimeException(e);
                } catch (InstantiationException e) {
                    throw new RuntimeException(e);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                } catch (InvocationTargetException e) {
                    throw new RuntimeException(e);
                }
                listItem.setTag(map);
            }
            map.update(dto);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    
    @Override
    public T getItem(int position) {
        try {
            return mQuery.mapRow(new AndroidDatabaseResults((Cursor) super.getItem(position), cache));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    
    public Context getContext() {
        return context;
    }
    
    public abstract class Mapper {
        Mapper () {}
        public abstract void setView(View row);
        public abstract void update(T obj);
    }
}