package io.github.jan.discordkm.internal.utils

import kotlin.coroutines.coroutineContext
import kotlin.reflect.KSuspendFunction0
import kotlin.reflect.KSuspendFunction1
import kotlin.reflect.KSuspendFunction2
import kotlin.reflect.KSuspendFunction3
import kotlin.reflect.KSuspendFunction4
import kotlin.reflect.KSuspendFunction5
import kotlin.reflect.KSuspendFunction6
import kotlin.reflect.KSuspendFunction7
import kotlin.reflect.KSuspendFunction8
import kotlin.reflect.KSuspendFunction9

suspend fun <T> KSuspendFunction0<T>.async() = com.soywiz.korio.async.async(coroutineContext, ::invoke)

suspend fun <A, B>KSuspendFunction1<A, B>.async(arg: A) = com.soywiz.korio.async.async(coroutineContext) { invoke(arg) }

suspend fun <A, B, C>KSuspendFunction2<A, B, C>.async(arg1: A, arg2: B) = com.soywiz.korio.async.async(coroutineContext) { invoke(arg1, arg2) }

suspend fun <A, B, C, D>KSuspendFunction3<A, B, C, D>.async(arg1: A, arg2: B, arg3: C) = com.soywiz.korio.async.async(coroutineContext) { invoke(arg1, arg2, arg3) }

suspend fun <A, B, C, D, E>KSuspendFunction4<A, B, C, D, E>.async(arg1: A, arg2: B, arg3: C, arg4: D) = com.soywiz.korio.async.async(coroutineContext) { invoke(arg1, arg2, arg3, arg4) }

suspend fun <A, B, C, D, E, F>KSuspendFunction5<A, B, C, D, E, F>.async(arg1: A, arg2: B, arg3: C, arg4: D, arg5: E) = com.soywiz.korio.async.async(coroutineContext) { invoke(arg1, arg2, arg3, arg4, arg5) }

suspend fun <A, B, C, D, E, F, G>KSuspendFunction6<A, B, C, D, E, F, G>.async(arg1: A, arg2: B, arg3: C, arg4: D, arg5: E, arg6: F) = com.soywiz.korio.async.async(coroutineContext) { invoke(arg1, arg2, arg3, arg4, arg5, arg6) }

suspend fun <A, B, C, D, E, F, G, H>KSuspendFunction7<A, B, C, D, E, F, G, H>.async(arg1: A, arg2: B, arg3: C, arg4: D, arg5: E, arg6: F, arg7: G) = com.soywiz.korio.async.async(coroutineContext) { invoke(arg1, arg2, arg3, arg4, arg5, arg6, arg7) }

suspend fun <A, B, C, D, E, F, G, H, I>KSuspendFunction8<A, B, C, D, E, F, G, H, I>.async(arg1: A, arg2: B, arg3: C, arg4: D, arg5: E, arg6: F, arg7: G, arg8: H) = com.soywiz.korio.async.async(coroutineContext) { invoke(arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8) }

suspend fun <A, B, C, D, E, F, G, H, I, J>KSuspendFunction9<A, B, C, D, E, F, G, H, I, J>.async(arg1: A, arg2: B, arg3: C, arg4: D, arg5: E, arg6: F, arg7: G, arg8: H, arg9: I) = com.soywiz.korio.async.async(coroutineContext) { invoke(arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9) }

