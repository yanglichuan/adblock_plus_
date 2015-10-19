/*
 * This file is part of Adblock Plus <https://adblockplus.org/>,
 * Copyright (C) 2006-2015 Eyeo GmbH
 *
 * Adblock Plus is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3 as
 * published by the Free Software Foundation.
 *
 * Adblock Plus is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Adblock Plus.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.adblockplus.android;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.adblockplus.android.updater.UpdaterActivity;
import org.adblockplus.libadblockplus.JsValue;
import org.adblockplus.libadblockplus.Subscription;
import org.apache.commons.lang.StringUtils;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

public final class Utils
{
  private Utils()
  {
    //
  }

  public static String getTag(final Class<?> clazz)
  {
    return clazz.getSimpleName();
  }

  public static String capitalizeString(final String s)
  {
    if (s == null || s.length() == 0)
    {
      return "";
    }

    final char first = s.charAt(0);

    return Character.isUpperCase(first) ? s : Character.toUpperCase(first) + s.substring(1);
  }

  protected static void showUpdateNotification(final Context context, final String url,
                                               final String error)
  {
    final NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
    builder.setContentTitle(context.getText(R.string.app_name));
    builder.setSmallIcon(R.drawable.ic_stat_warning);
    builder.setAutoCancel(true);
    builder.setOnlyAlertOnce(true);
    final PendingIntent emptyIntent = PendingIntent.getActivity(context, 0, new Intent(), 0);
    builder.setContentIntent(emptyIntent);

    if (StringUtils.isNotEmpty(error))
    {
      builder.setContentText(context.getString(R.string.msg_update_fail));
    }
    else if (StringUtils.isNotEmpty(url))
    {
      builder.setSmallIcon(R.drawable.ic_stat_download);
      final Intent intent = new Intent(context, UpdaterActivity.class)
          .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
      intent.setAction("download");
      intent.putExtra("url", url);
      final PendingIntent updateIntent =
          PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
      builder.setContentIntent(updateIntent);
      builder.setContentText(context.getString(R.string.msg_update_available));
    }
    else
    {
      builder.setContentText(context.getString(R.string.msg_update_missing));
    }

    final Notification notification = builder.getNotification();
    final NotificationManager notificationManager =
        (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    notificationManager.notify(AdblockPlus.UPDATE_NOTIFICATION_ID, notification);
  }

  protected static void updateSubscriptionStatus(final Context context, final Subscription sub)
  {
    final JsValue jsDownloadStatus = sub.getProperty("downloadStatus");
    final String downloadStatus = jsDownloadStatus.isNull() ? "" : jsDownloadStatus.toString();
    final long lastDownload = sub.getProperty("lastDownload").asLong();

    String status = "synchronize_never";
    long time = 0;

    if (sub.isUpdating())
    {
      status = "synchronize_in_progress";
    }
    else if (StringUtils.isNotEmpty(downloadStatus) && !downloadStatus.equals("synchronize_ok"))
    {
      status = downloadStatus;
    }
    else if (lastDownload > 0)
    {
      time = lastDownload;
      status = "synchronize_last_at";
    }

    context.sendBroadcast(new Intent(AdblockPlus.BROADCAST_SUBSCRIPTION_STATUS)
        .putExtra("url", sub.getProperty("url").toString())
        .putExtra("status", status)
        .putExtra("time", time * 1000L));
  }

  public static void appendRawTextFile(final Context context, final StringBuilder text, final int id)
  {
    try
    {
      final BufferedReader buf = new BufferedReader(new InputStreamReader(context.getResources().openRawResource(id)));

      try
      {
        String line;
        while ((line = buf.readLine()) != null)
        {
          text.append(line);
          text.append('\n');
        }
      }
      finally
      {
        buf.close();
      }

    }
    catch (final Exception e)
    {
      // Ignored for now
    }
  }
}
