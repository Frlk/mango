/*
 * Copyright 2014 mango.jfaster.org
 *
 * The Mango Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package org.jfaster.mango.operator;

import org.jfaster.mango.datasource.factory.SimpleDataSourceFactory;
import org.jfaster.mango.jdbc.GeneratedKeyHolder;
import org.jfaster.mango.support.*;
import org.jfaster.mango.support.model4table.User;
import org.jfaster.mango.util.reflect.MethodDescriptor;
import org.jfaster.mango.util.reflect.ParameterDescriptor;
import org.jfaster.mango.util.reflect.TypeToken;
import org.junit.Test;

import javax.sql.DataSource;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

/**
 * @author ash
 */
public class UpdateOperatorTest {

    @Test
    public void testUpdate() throws Exception {
        TypeToken<User> pt = TypeToken.of(User.class);
        TypeToken<Integer> rt = TypeToken.of(int.class);
        String srcSql = "update user set name=:1.name where id=:1.id";
        Operator operator = getOperator(pt, rt, srcSql);

        StatsCounter sc = new StatsCounter();
        operator.setStatsCounter(sc);
        operator.setJdbcOperations(new JdbcOperationsAdapter() {
            @Override
            public int update(DataSource ds, String sql, Object[] args) {
                String descSql = "update user set name=? where id=?";
                assertThat(sql, equalTo(descSql));
                assertThat(args.length, equalTo(2));
                assertThat(args[0], equalTo((Object) "ash"));
                assertThat(args[1], equalTo((Object) 100));
                return 1;
            }
        });

        User user = new User();
        user.setId(100);
        user.setName("ash");
        Object r = operator.execute(new Object[]{user});
        assertThat(r.getClass().equals(Integer.class), is(true));
    }

    @Test
    public void testUpdateReturnGeneratedIdInt() throws Exception {
        TypeToken<User> pt = TypeToken.of(User.class);
        TypeToken<Integer> rt = TypeToken.of(int.class);
        String srcSql = "insert into user(id, name) values(:1.id, :1.name)";
        Operator operator = getOperatorReturnGeneratedId(pt, rt, srcSql);

        StatsCounter sc = new StatsCounter();
        operator.setStatsCounter(sc);
        operator.setJdbcOperations(new JdbcOperationsAdapter() {
            @Override
            public int update(DataSource ds, String sql, Object[] args, GeneratedKeyHolder holder) {
                String descSql = "insert into user(id, name) values(?, ?)";
                assertThat(sql, equalTo(descSql));
                assertThat(args.length, equalTo(2));
                assertThat(args[0], equalTo((Object) 100));
                assertThat(args[1], equalTo((Object) "ash"));
                assertThat(holder.getKeyClass().equals(int.class), is(true));
                holder.setKey(100);
                return 1;
            }
        });

        User user = new User();
        user.setId(100);
        user.setName("ash");
        Object r = operator.execute(new Object[]{user});
        assertThat(r.getClass().equals(Integer.class), is(true));
    }

    @Test
    public void testUpdateReturnGeneratedIdLong() throws Exception {
        TypeToken<User> pt = TypeToken.of(User.class);
        TypeToken<Long> rt = TypeToken.of(long.class);
        String srcSql = "insert into user(id, name) values(:1.id, :1.name)";
        Operator operator = getOperatorReturnGeneratedId(pt, rt, srcSql);

        StatsCounter sc = new StatsCounter();
        operator.setStatsCounter(sc);
        operator.setJdbcOperations(new JdbcOperationsAdapter() {
            @Override
            public int update(DataSource ds, String sql, Object[] args, GeneratedKeyHolder holder) {
                String descSql = "insert into user(id, name) values(?, ?)";
                assertThat(sql, equalTo(descSql));
                assertThat(args.length, equalTo(2));
                assertThat(args[0], equalTo((Object) 100));
                assertThat(args[1], equalTo((Object) "ash"));
                assertThat(holder.getKeyClass().equals(long.class), is(true));
                holder.setKey(100L);
                return 1;
            }
        });

        User user = new User();
        user.setId(100);
        user.setName("ash");
        Object r = operator.execute(new Object[]{user});
        assertThat(r.getClass().equals(Long.class), is(true));
    }

    @Test
    public void testStatsCounter() throws Exception {
        TypeToken<User> pt = TypeToken.of(User.class);
        TypeToken<Integer> rt = TypeToken.of(int.class);
        String srcSql = "update user set name=:1.name where id=:1.id";
        Operator operator = getOperator(pt, rt, srcSql);

        StatsCounter sc = new StatsCounter();
        operator.setStatsCounter(sc);
        operator.setJdbcOperations(new JdbcOperationsAdapter() {
            @Override
            public int update(DataSource ds, String sql, Object[] args) {
                String descSql = "update user set name=? where id=?";
                assertThat(sql, equalTo(descSql));
                assertThat(args.length, equalTo(2));
                assertThat(args[0], equalTo((Object) "ash"));
                assertThat(args[1], equalTo((Object) 100));
                return 1;
            }
        });

        User user = new User();
        user.setId(100);
        user.setName("ash");
        operator.execute(new Object[]{user});
        assertThat(sc.snapshot().executeSuccessCount(), equalTo(1L));
        operator.execute(new Object[]{user});
        assertThat(sc.snapshot().executeSuccessCount(), equalTo(2L));

        operator.setJdbcOperations(new JdbcOperationsAdapter());
        try {
            operator.execute(new Object[]{user});
        } catch (UnsupportedOperationException e) {
        }
        assertThat(sc.snapshot().executeExceptionCount(), equalTo(1L));
        try {
            operator.execute(new Object[]{user});
        } catch (UnsupportedOperationException e) {
        }
        assertThat(sc.snapshot().executeExceptionCount(), equalTo(2L));
    }

    private Operator getOperator(TypeToken<?> pt, TypeToken<?> rt, String srcSql) throws Exception {
        List<Annotation> empty = Collections.emptyList();
        ParameterDescriptor p = new ParameterDescriptor(0, pt.getType(), pt.getRawType(), empty);
        List<ParameterDescriptor> pds = Arrays.asList(p);

        List<Annotation> methodAnnos = new ArrayList<Annotation>();
        methodAnnos.add(new MockDB());
        methodAnnos.add(new MockSQL(srcSql));
        MethodDescriptor md = new MethodDescriptor(rt.getType(), rt.getRawType(), methodAnnos, pds);

        OperatorFactory factory = new OperatorFactory(
                new SimpleDataSourceFactory(Config.getDataSource()),
                null, new InterceptorChain(), new InterceptorChain());

        Operator query = factory.getOperator(md);
        return query;
    }

    private Operator getOperatorReturnGeneratedId(TypeToken<?> pt, TypeToken<?> rt, String srcSql) throws Exception {
        List<Annotation> empty = Collections.emptyList();
        ParameterDescriptor p = new ParameterDescriptor(0, pt.getType(), pt.getRawType(), empty);
        List<ParameterDescriptor> pds = Arrays.asList(p);

        List<Annotation> methodAnnos = new ArrayList<Annotation>();
        methodAnnos.add(new MockDB());
        methodAnnos.add(new MockSQL(srcSql));
        methodAnnos.add(new MockReturnGeneratedId());
        MethodDescriptor md = new MethodDescriptor(rt.getType(), rt.getRawType(), methodAnnos, pds);

        OperatorFactory factory = new OperatorFactory(
                new SimpleDataSourceFactory(Config.getDataSource()),
                null, new InterceptorChain(), new InterceptorChain());

        Operator query = factory.getOperator(md);
        return query;
    }

}