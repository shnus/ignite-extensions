/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.ignite.gatling.action.cache

import io.gatling.core.action.Action
import io.gatling.core.session.Expression
import io.gatling.core.session.Session
import io.gatling.core.structure.ScenarioContext
import org.apache.ignite.gatling.Predef.IgniteCheck
import org.apache.ignite.gatling.action.CacheAction

/**
 * Action for the getAndPut Ignite operation.
 *
 * @tparam K Type of the cache key.
 * @tparam V Type of the cache value.
 * @param requestName Name of the request.
 * @param cacheName Name of cache.
 * @param key Cache entry key.
 * @param value Cache entry value.
 * @param keepBinary True if it should operate with binary objects.
 * @param async True if async API should be used.
 * @param checks Collection of checks to perform against the operation result.
 * @param next Next action from chain to invoke upon this one completion.
 * @param ctx Scenario context.
 */
class CacheGetAndPutAction[K, V](
    requestName: String,
    cacheName: String,
    key: Expression[K],
    value: Expression[V],
    keepBinary: Boolean,
    async: Boolean,
    checks: Seq[IgniteCheck[K, V]],
    next: Action,
    ctx: ScenarioContext
) extends CacheAction[K, V]("getAndPut", requestName, ctx, next, cacheName, keepBinary, async) {

    override protected def execute(session: Session): Unit = withSessionCheck(session) {
        for {
            CacheActionParameters(cacheApi, _) <- resolveCacheParameters(session)

            resolvedKey <- key(session)

            resolvedValue <- value(session)
        } yield {
            logger.debug(s"session user id: #${session.userId}, before $request")

            val func = if (async) cacheApi.getAndPutAsync _ else cacheApi.getAndPut _

            call(func(resolvedKey, resolvedValue), session, checks)
        }
    }
}
