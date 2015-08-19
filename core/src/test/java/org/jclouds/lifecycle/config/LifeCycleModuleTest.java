/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jclouds.lifecycle.config;

import static com.google.inject.name.Names.named;
import static org.assertj.core.api.Assertions.assertThat;
import static org.jclouds.Constants.PROPERTY_USER_THREADS;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;

import javax.annotation.PostConstruct;

import static org.easymock.EasyMock.createStrictMock;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import org.jclouds.concurrent.config.ExecutorServiceModule;
import org.jclouds.lifecycle.Closer;
import org.testng.annotations.Test;

import com.google.common.util.concurrent.ExecutionList;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;

@Test
public class LifeCycleModuleTest {

   @Test
   void testBindsExecutor() {
      Injector i = createInjector();
      assertThat(i.getInstance(Key.get(ListeningExecutorService.class, named(PROPERTY_USER_THREADS))) != null).isTrue();
   }

   private Injector createInjector() {
      Injector i = Guice.createInjector(new AbstractModule() {
         protected void configure() {
            bindConstant().annotatedWith(named(PROPERTY_USER_THREADS)).to(1);
         }
      }, new LifeCycleModule(), new ExecutorServiceModule());
      // TODO: currently have to manually invoke the execution list, as otherwise it may occur
      // before everything is wired up
      i.getInstance(ExecutionList.class).execute();
      return i;
   }

   @Test
   void testBindsCloser() {
      Injector i = createInjector();
      assertThat(i.getInstance(Closer.class) != null).isTrue();
      assertThat(i.getInstance(Closer.class).getState() == Closer.State.AVAILABLE).isTrue();
   }

   @Test
   void testCloserClosesExecutor() throws IOException {
      Injector i = createInjector();
      ListeningExecutorService executor = i.getInstance(Key.get(ListeningExecutorService.class,
            named(PROPERTY_USER_THREADS)));
      assertThat(!executor.isShutdown()).isTrue();
      Closer closer = i.getInstance(Closer.class);
      assertThat(closer.getState() == Closer.State.AVAILABLE).isTrue();
      closer.close();
      assertThat(executor.isShutdown()).isTrue();
      assertThat(closer.getState() == Closer.State.DONE).isTrue();
   }

   @Test
   void testCloserPreDestroyOrder() throws IOException {
      Injector i = createInjector();
      ListeningExecutorService userExecutor = i.getInstance(Key.get(ListeningExecutorService.class,
            named(PROPERTY_USER_THREADS)));
      assertThat(!userExecutor.isShutdown()).isTrue();
      Closer closer = i.getInstance(Closer.class);
      assertThat(closer.getState() == Closer.State.AVAILABLE).isTrue();
      closer.close();
      assertThat(userExecutor.isShutdown()).isTrue();
      assertThat(closer.getState() == Closer.State.DONE).isTrue();
   }

   static class PostConstructable {
      boolean isStarted;

      @PostConstruct
      void start() {
         isStarted = true;
      }
   }

   @Test
   void testPostConstruct() {
      Injector i = createInjector().createChildInjector(new AbstractModule() {
         protected void configure() {
            bind(PostConstructable.class);
         }
      });
      PostConstructable postConstructable = i.getInstance(PostConstructable.class);
      assertThat(postConstructable.isStarted).isTrue();
   }

   @Test
   void testCloserClosingState() throws InterruptedException {
      Injector i = createInjector();
      final Closer closer = i.getInstance(Closer.class);

      final CountDownLatch closeDone = new CountDownLatch(1);
      final CountDownLatch closeStart = new CountDownLatch(1);

      closer.addToClose(new Closeable() {
         @Override
         public void close() throws IOException {
             try {
                 closeStart.countDown();
                 assertThat(closer.getState() == Closer.State.PROCESSING).isTrue();
                 closeDone.await();
             } catch (InterruptedException e) {
                 assertThat(false).as(e.getMessage()).isTrue();
             }
         }
      });

      Thread thread = new Thread(new Runnable() {
         @Override
         public void run() {
            try {
               closer.close();
            } catch (IOException e) {
               assertThat(false).as(e.getMessage()).isTrue();
            }
         }
      });

      thread.start();

      closeStart.await();

      assertThat(closer.getState() == Closer.State.PROCESSING).isTrue();

      closeDone.countDown();

      thread.join();

      assertThat(closer.getState() == Closer.State.DONE).isTrue();
   }

   @Test
   void testCloserCallOneClose() throws IOException, InterruptedException {
      Injector i = createInjector();
      final Closer closer = i.getInstance(Closer.class);

      Closeable closeable = createStrictMock(Closeable.class);

      closeable.close();

      expectLastCall();

      replay(closeable);

      closer.addToClose(closeable);

      Runnable closeContext = new Runnable() {
         @Override
         public void run() {
            try {
               closer.close();
            } catch (IOException e) {
               assertThat(false).as(e.getMessage()).isTrue();
            }
         }
      };

      Thread thread1 = new Thread(closeContext);
      Thread thread2 = new Thread(closeContext);

      thread1.start();
      thread2.start();

      thread1.join();
      thread2.join();

      verify(closeable);

      assertThat(closer.getState() == Closer.State.DONE).isTrue();
   }
}
