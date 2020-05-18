package com.grey.gxplayer.models;

import com.grey.gxplayer.util.MyUtil;

public class PlayBackTrack {
    public long mId;
    public long sourceId;
    public MyUtil.IdType mIdType;
    public int mCurrentPos;

    public PlayBackTrack(long mId, long sourceId, MyUtil.IdType mIdType, int mCurrentPos) {
        this.mId = mId;
        this.sourceId = sourceId;
        this.mIdType = mIdType;
        this.mCurrentPos = mCurrentPos;
    }
}
