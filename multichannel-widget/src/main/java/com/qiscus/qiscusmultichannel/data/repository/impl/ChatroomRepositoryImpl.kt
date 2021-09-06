package com.qiscus.qiscusmultichannel.data.repository.impl

import com.qiscus.qiscusmultichannel.QiscusMultichannelWidget
import com.qiscus.qiscusmultichannel.QiscusMultichannelWidgetConfig
import com.qiscus.qiscusmultichannel.data.model.DataInitialChat
import com.qiscus.qiscusmultichannel.data.model.user.UserProperties
import com.qiscus.qiscusmultichannel.data.repository.ChatroomRepository
import com.qiscus.qiscusmultichannel.data.repository.response.ResponseInitiateChat
import com.qiscus.qiscusmultichannel.util.Const
import com.qiscus.qiscusmultichannel.util.QiscusChatLocal
import com.qiscus.sdk.chat.core.data.model.QAccount
import com.qiscus.sdk.chat.core.data.model.QMessage
import com.qiscus.sdk.chat.core.data.model.QUser
import org.json.JSONObject
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers

/**
 * Created on : 05/08/19
 * Author     : Taufik Budi S
 * GitHub     : https://github.com/tfkbudi
 */
class ChatroomRepositoryImpl : ChatroomRepository {

    fun sendComment(
        roomId: Long,
        message: QMessage,
        onSuccess: (QMessage) -> Unit,
        onError: (Throwable) -> Unit
    ) {
        val qAccount: QAccount = Const.qiscusCore()?.qiscusAccount!!
        val qUser = QUser()
        qUser.avatarUrl = qAccount.avatarUrl
        qUser.id = qAccount.id
        qUser.extras = qAccount.extras
        qUser.name = qAccount.name
        message.sender = qUser

        if (message.type == QMessage.Type.TEXT && message.text.trim().isEmpty()) {
            onError(Throwable("message can't empty"))
        }

        Const.qiscusCore()?.api?.sendMessage(message)
            ?.doOnSubscribe { Const.qiscusCore()?.dataStore?.addOrUpdate(message) }
            ?.subscribeOn(Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe({
                if (it.chatRoomId == roomId) {
                    onSuccess(it)
                }
            }, { throwable ->
                throwable.printStackTrace()
                if (message.chatRoomId == roomId) {
                    onError(throwable)
                }
            })
    }

    fun publishCustomEvent(
        roomId: Long,
        data: JSONObject
    ) {
        Const.qiscusCore()?.pusherApi?.publishCustomEvent(roomId, data)
    }

    fun subscribeCustomEvent(
        roomId: Long
    ) {
        Const.qiscusCore()?.pusherApi?.subsribeCustomEvent(roomId)
    }

    fun loginMultichannel(
        name: String?,
        userId: String?,
        avatar: String?,
        extras: String?,
        userProp: List<UserProperties>?,
        config: QiscusMultichannelWidgetConfig?,
        responseInitiateChat: (ResponseInitiateChat) -> Unit,
        onError: (Throwable) -> Unit
    ) {

        Const.qiscusCore()?.api?.jwtNonce
            ?.doOnNext {
                QiscusChatLocal.saveExtras(extras)
                QiscusChatLocal.saveUserProps(userProp)
                QiscusChatLocal.saveUserId(userId)
                QiscusChatLocal.saveAvatar(avatar)
            }
            ?.subscribeOn(Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe({
                QiscusMultichannelWidget.instance.component.qiscusChatRepository.initiateChat(
                    DataInitialChat(
                        Const.qiscusCore()?.appId!!,
                        userId,
                        name,
                        avatar,
                        it.nonce,
                        null,
                        extras,
                        userProp
                    ), { response ->
                        response.data.isSessional?.let { sessional ->
                            config?.setSessional(sessional)
                        }
                        responseInitiateChat(response)
                    }, { throwable ->
                        onError(throwable)
                    })
            }, {
                onError(it)
            })
    }
}