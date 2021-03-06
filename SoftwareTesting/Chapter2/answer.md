# 选择题

# 1-5 BBBDC

# 6-10 DABCC
 
# 简答题
# 1
- 测试计划：根据用户需求报告中关于功能要求和性能指标的规格说明书，定义相应的测试需求报告，选择测试内容，合理安排测试人员，测试时间及测试资源等
- 测试设计：将测试计划阶段制订的测试需求分解，细化为若干个可执行的测试过程，并为每个测试过程选择适当的测试用例，保证测试结果的有效性
- 测试执行：执行测试开发阶段建立的自动测试过程，并对所发现的缺陷进行跟踪管理。测试执行一般由单元测试、集成测试、确认测试以及回归测试等步骤组成
- 测试评估：结合量化的测试覆盖率以及缺陷跟踪报告，对于应用软件的质量和开发团队的工作进度以及工作效率进行综合评价

# 2 
- 测试的单元不同：单元测试是针对软件的基本单元（如函数）所做的测试，而集成测试是以模块和子系统为单位进行的测试，主要测试接口间的关系
- 测试依据不同：单元测试是针对软件详细设计做的测试，测试用例主要依据的是详细设计，而集成测试是针对高层（概要）设计做的测试，测试用例主要依据是概要设计
- 测试空间不同：集成测试主要测试的是接口层的测试空间，与单元测试不同，不关心内部实现层的测试空间
- 测试方法不同：集成测试关注的是接口的集成，单元测试关注单个单元，因此具体的测试方法上不会相同

# 3
- 测试性质不同：单元测试属于白盒测试，而系统测试属于黑盒测试
- 时期不同：单元测试是早期的测试，发现问题可以较早定位，而系统测试是后期测试，发现错误后定位工作比较困难

# 4 
- 依据不同：集成测试主要依据是概要设计说明书，系统测试主要依据是需求设计说明书
- 测试对象不同：集成测试是系统模块的测试，系统测试是对整个系统的测试，包括相关软硬件平台等

# 5 
- 大爆炸集成：又称为一次性组装或整体拼装，属于非增值集成，这种集成策略的做法就是把所有通过单元测试的模块一次性集成到一起进行测试，不考虑组件之间的相互依赖性以及可能存在的风险
- 三明治集成：一种混合增量式测试策略
- 自顶向下集成：就是按照系统层次的结构，以主程序模块为中心，自上而下对各个模块一边组装一边测试
- 自底向上集成：从依赖性最小的底层模块开始，逐层向上集成
- 高频集成：与软件开发过程同步，每隔一段时间堆开发团队的现有代码进行一次集成测试

# 6 
- `alpha`测试：在开发环境下或者公司内部用户在模拟实际操作，由用户参与的测试，测试的目的是评价软件产品的功能、可使用性、可靠性、性能等
- `beta`测试：在实际使用环境下的测试，开发者通常不在现场，由用户记录遇到的所有问题，定期向开发者报告，开发者综合用户的报告后做出修改

# 7
- 在修改范围内的测试
- 在受影响范围内的测试
- 根据一定的覆盖率指标选择回归测试
- 基于操作剖面测试
- 基于风险选择测试
