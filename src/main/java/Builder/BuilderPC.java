package Builder;

public class BuilderPC {

    private String cpu;
    private String ram;
    private String storage;
    private String gpu;


    private BuilderPC (Builder builder){
        this.cpu = builder.cpu;
        this.ram = builder.ram;
        this.storage = builder.storage;
        this.gpu = builder.gpu;
    }

    public void infoPc(){
        System.out.println("Проц: "+ this.cpu );
        System.out.println(this.ram);
        System.out.println(this.storage);
        System.out.println(this.gpu);
    }

    public static class Builder{
        private String cpu;
        private String ram;
        private String storage;
        private String gpu;

        public Builder cpu (String cpu){
            this.cpu = cpu;
            return this;
        }
        public Builder ram (String ram){
            this.ram = ram;
            return this;
        }
        public Builder storage (String storage){
            this.storage = storage;
            return this;
        }
        public Builder gpu (String gpu){
            this.gpu = gpu;
            return this;
        }
        public BuilderPC build (){
            return new BuilderPC(this);
        }


    }

    public static void main(String[] args) {
        BuilderPC gaming = new BuilderPC.Builder()
                .cpu("Intel i7")
                .ram("16GB")
                .storage("1TB SSD")
                .gpu("rx666666xxl")
                .build();
        gaming.infoPc();
    }
}
