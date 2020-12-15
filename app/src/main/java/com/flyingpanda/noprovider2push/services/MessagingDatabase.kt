package com.flyingpanda.noprovider2push.services

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

private const val DB_NAME = "gotify_service"
private const val DB_VERSION = 1

class MessagingDatabase(context: Context) : SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION){
    private val CREATE_TABLE_APPS = "CREATE TABLE apps (" +
            "package_name TEXT," +
            "uid INT," +
            "service_name TEXT," +
            "PRIMARY KEY (package_name));"
    private val TABLE_APPS = "apps"
    private val FIELD_PACKAGE_NAME = "package_name"
    private val FIELD_UID = "uid"
    private val FIELD_SERVICE_NAME = "service_name"

    override fun onCreate(db: SQLiteDatabase){
        db.execSQL(CREATE_TABLE_APPS)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        throw IllegalStateException("Upgrades not supported")
    }

    fun registerApp(packageName: String, uid: Int, serviceName: String){
        val db = writableDatabase
        val values = ContentValues().apply {
            put(FIELD_PACKAGE_NAME, packageName)
            put(FIELD_UID, uid.toString())
            put(FIELD_SERVICE_NAME,serviceName)
        }
        db.insert(TABLE_APPS,null,values)
    }

    fun unregisterApp(packageName: String, uid: Int){
        val db = writableDatabase
        val selection = "$FIELD_PACKAGE_NAME = ? AND $FIELD_UID = ?"
        val selectionArgs = arrayOf(packageName,uid.toString())
        db.delete(TABLE_APPS,selection,selectionArgs)
    }

    fun forceUnregisterApp(packageName: String){
        val db = writableDatabase
        val selection = "$FIELD_PACKAGE_NAME = ?"
        val selectionArgs = arrayOf(packageName)
        db.delete(TABLE_APPS,selection,selectionArgs)
    }

    fun isRegistered(packageName: String): Boolean {
        val db = readableDatabase
        val selection = "$FIELD_PACKAGE_NAME = ?"
        val selectionArgs = arrayOf(packageName)
        return db.query(
                TABLE_APPS,
                null,
                selection,
                selectionArgs,
                null,
                null,
                null
        ).use { cursor ->
            (cursor != null && cursor.count > 0)
        }
    }

    fun strictIsRegistered(packageName: String, uid: Int): Boolean {
        val db = readableDatabase
        val selection = "$FIELD_PACKAGE_NAME = ? AND $FIELD_UID = ?"
        val selectionArgs = arrayOf(packageName,uid.toString())
        return db.query(
                TABLE_APPS,
                null,
                selection,
                selectionArgs,
                null,
                null,
                null
        ).use { cursor ->
            (cursor != null && cursor.count > 0)
        }
    }

    fun getServiceName(packageName: String): String{
        val db = readableDatabase
        val projection = arrayOf(FIELD_SERVICE_NAME)
        val selection = "$FIELD_PACKAGE_NAME = ?"
        val selectionArgs = arrayOf(packageName)
        return db.query(
                TABLE_APPS,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                null
        ).use { cursor ->
            if (cursor.moveToFirst()) cursor.getString(cursor.getColumnIndex(FIELD_SERVICE_NAME)) else ""
        }
    }

    fun listApps(): List<String>{
        val db = readableDatabase
        val projection = arrayOf(FIELD_PACKAGE_NAME)
        return db.query(
                TABLE_APPS,
                projection,
                null,
                null,
                null,
                null,
                null
        ).use{ cursor ->
            generateSequence { if (cursor.moveToNext()) cursor else null }
                    .map{ it.getString(it.getColumnIndex(FIELD_PACKAGE_NAME)) }
                    .toList()
        }
    }
}