package org.shoulder.autoconfigure.log.operation.async;
// 该包用于使得与创建日志实体的方法（添加 @OperationLog 注解的方法）不在统一线程时，仍可以使用操作日志框架的  OperationLogUtils 工具